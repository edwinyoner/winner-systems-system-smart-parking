import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

void main() {
  runApp(const SmartParkingApp());
}

class SmartParkingApp extends StatelessWidget {
  const SmartParkingApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'WINNER SYSTEMS - Smart Parking',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        brightness: Brightness.dark,
        primaryColor: const Color(0xFFFFC107),
        scaffoldBackgroundColor: const Color(0xFF0B0B0F),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFFFFC107),
          secondary: Color(0xFFFFC107),
          surface: Color(0xFF16161C),
        ),
        useMaterial3: true,
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF0B0B0F),
          elevation: 0,
          centerTitle: false,
        ),
        cardTheme: CardThemeData(
          color: const Color(0xFF16161C),
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: BorderSide(color: Colors.white.withOpacity(0.06)),
          ),
        ),
      ),
      home: const MainAuthGate(),
    );
  }
}

class MainAuthGate extends StatefulWidget {
  const MainAuthGate({super.key});

  @override
  State<MainAuthGate> createState() => _MainAuthGateState();
}

class _MainAuthGateState extends State<MainAuthGate> {
  final _storage = const FlutterSecureStorage();
  bool _isLoading = true;
  String? _jwtToken;

  @override
  void initState() {
    super.initState();
    _checkAuthStatus();
  }

  Future<void> _checkAuthStatus() async {
    try {
      final token = await _storage.read(key: 'auth_token');
      setState(() {
        _jwtToken = token;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Center(
          child: CircularProgressIndicator(color: Color(0xFFFFC107)),
        ),
      );
    }
    return _jwtToken != null
        ? const PrivateDashboardScreen()
        : const PublicParkingScreen();
  }
}

// =========================================================================
// 🔓 ACCESO PÚBLICO
// =========================================================================
class PublicParkingScreen extends StatefulWidget {
  const PublicParkingScreen({super.key});

  @override
  State<PublicParkingScreen> createState() => _PublicParkingScreenState();
}

class _PublicParkingScreenState extends State<PublicParkingScreen> {
  final String gatewayUrl =
      'http://localhost:8080/api/v1/public/parking/huaraz';

  late Future<List<dynamic>> _futureParqueos;

  @override
  void initState() {
    super.initState();
    _futureParqueos = _getParqueosDisponibles();
  }

  Future<List<dynamic>> _getParqueosDisponibles() async {
    try {
      final response = await http.get(Uri.parse(gatewayUrl));
      if (response.statusCode == 200) {
        return json.decode(response.body);
      }
    } catch (e) {
      debugPrint('Usando simulación local: $e');
    }
    return [
      {
        "zona": "Jr. San Martín C-1",
        "disponibles": 0,
        "total": 8,
        "estado": "Lleno",
      },
      {
        "zona": "Jr. Simón Bolívar C-3",
        "disponibles": 5,
        "total": 12,
        "estado": "Disponible",
      },
      {
        "zona": "Jr. Dámaso Antúnez C-4",
        "disponibles": 3,
        "total": 10,
        "estado": "Disponible",
      },
      {
        "zona": "Av. Luzuriaga C-2",
        "disponibles": 7,
        "total": 15,
        "estado": "Disponible",
      },
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Header premium
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 12, 8),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFC107).withOpacity(0.15),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(
                      Icons.local_parking_rounded,
                      color: Color(0xFFFFC107),
                      size: 28,
                    ),
                  ),
                  const SizedBox(width: 14),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Smart Parking',
                          style: TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.5,
                          ),
                        ),
                        Text(
                          'Huaraz • Tiempo real',
                          style: TextStyle(
                            fontSize: 13,
                            color: Colors.white54,
                          ),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const LoginGoogleScreen(),
                        ),
                      );
                    },
                    icon: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.08),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(
                        Icons.login_rounded,
                        color: Color(0xFFFFC107),
                      ),
                    ),
                  ),
                ],
              ),
            ),

            // Título de sección
            const Padding(
              padding: EdgeInsets.fromLTRB(20, 12, 20, 8),
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  'Disponibilidad actual',
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                    color: Colors.white70,
                  ),
                ),
              ),
            ),

            Expanded(
              child: FutureBuilder<List<dynamic>>(
                future: _futureParqueos,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Center(
                      child: CircularProgressIndicator(
                        color: Color(0xFFFFC107),
                      ),
                    );
                  }

                  final parqueos = snapshot.data ?? [];

                  return RefreshIndicator(
                    color: const Color(0xFFFFC107),
                    backgroundColor: const Color(0xFF16161C),
                    onRefresh: () async {
                      setState(() {
                        _futureParqueos = _getParqueosDisponibles();
                      });
                      await _futureParqueos;
                    },
                    child: ListView.builder(
                      padding: const EdgeInsets.fromLTRB(16, 4, 16, 24),
                      itemCount: parqueos.length,
                      itemBuilder: (context, index) {
                        final item = parqueos[index];
                        final int disponibles = item['disponibles'] ?? 0;
                        final int total = item['total'] ?? 1;
                        final bool isLleno = disponibles == 0;
                        final double ocupacion =
                            1 - (disponibles / total);

                        return Container(
                          margin: const EdgeInsets.only(bottom: 14),
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: const Color(0xFF16161C),
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(
                              color: Colors.white.withOpacity(0.06),
                            ),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    width: 48,
                                    height: 48,
                                    decoration: BoxDecoration(
                                      color: isLleno
                                          ? Colors.red.withOpacity(0.15)
                                          : Colors.green.withOpacity(0.15),
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                    child: Icon(
                                      Icons.local_parking_rounded,
                                      color: isLleno
                                          ? Colors.redAccent
                                          : Colors.greenAccent,
                                      size: 26,
                                    ),
                                  ),
                                  const SizedBox(width: 14),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          item['zona'],
                                          style: const TextStyle(
                                            fontSize: 16,
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                        const SizedBox(height: 4),
                                        Text(
                                          '$disponibles de $total espacios libres',
                                          style: const TextStyle(
                                            fontSize: 13,
                                            color: Colors.white54,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 12,
                                      vertical: 6,
                                    ),
                                    decoration: BoxDecoration(
                                      color: isLleno
                                          ? Colors.red.withOpacity(0.15)
                                          : Colors.green.withOpacity(0.15),
                                      borderRadius: BorderRadius.circular(20),
                                    ),
                                    child: Text(
                                      item['estado'],
                                      style: TextStyle(
                                        color: isLleno
                                            ? Colors.redAccent
                                            : Colors.greenAccent,
                                        fontWeight: FontWeight.bold,
                                        fontSize: 12,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 14),
                              // Barra de ocupación
                              ClipRRect(
                                borderRadius: BorderRadius.circular(6),
                                child: LinearProgressIndicator(
                                  value: ocupacion,
                                  minHeight: 6,
                                  backgroundColor:
                                      Colors.white.withOpacity(0.08),
                                  valueColor: AlwaysStoppedAnimation<Color>(
                                    isLleno
                                        ? Colors.redAccent
                                        : const Color(0xFFFFC107),
                                  ),
                                ),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                'Ocupación: ${(ocupacion * 100).toStringAsFixed(0)}%',
                                style: const TextStyle(
                                  fontSize: 11,
                                  color: Colors.white38,
                                ),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// =========================================================================
// 🔐 LOGIN CON GOOGLE
// =========================================================================
class LoginGoogleScreen extends StatelessWidget {
  const LoginGoogleScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 28),
          child: Column(
            children: [
              Align(
                alignment: Alignment.centerLeft,
                child: IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.arrow_back_ios_new_rounded),
                ),
              ),
              const Spacer(flex: 2),
              // Logo / Icono principal
              Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [
                      const Color(0xFFFFC107).withOpacity(0.25),
                      const Color(0xFFFFC107).withOpacity(0.05),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: const Color(0xFFFFC107).withOpacity(0.3),
                  ),
                ),
                child: const Icon(
                  Icons.directions_car_filled_rounded,
                  size: 52,
                  color: Color(0xFFFFC107),
                ),
              ),
              const SizedBox(height: 28),
              const Text(
                'SMART PARKING',
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.5,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Huaraz • Winner Systems',
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.white54,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Accede para ver tus registros\ny sincronizar vehículos en tiempo real',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.white38,
                  height: 1.4,
                ),
              ),
              const Spacer(flex: 3),

              // Botón Google oficial
              ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  foregroundColor: Colors.black87,
                  elevation: 0,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                onPressed: () async {
                  try {
                    const storage = FlutterSecureStorage();
                    await storage.write(
                      key: 'auth_token',
                      value: 'MOCK_JWT_TOKEN',
                    );
                  } catch (_) {}

                  if (context.mounted) {
                    Navigator.pushAndRemoveUntil(
                      context,
                      MaterialPageRoute(
                        builder: (_) => const PrivateDashboardScreen(),
                      ),
                      (route) => false,
                    );
                  }
                },
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    // Logo de Google (red)
                    Image.network(
                      'https://developers.google.com/identity/images/g-logo.png',
                      height: 22,
                      width: 22,
                      errorBuilder: (_, __, ___) => const Text(
                        'G',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: Colors.red,
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    const Text(
                      'Continuar con Google',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
              const Text(
                'Al continuar aceptas los términos de uso',
                style: TextStyle(fontSize: 12, color: Colors.white30),
              ),
              const Spacer(flex: 2),
            ],
          ),
        ),
      ),
    );
  }
}

// =========================================================================
// 🔐 ZONA PRIVADA
// =========================================================================
class PrivateDashboardScreen extends StatefulWidget {
  const PrivateDashboardScreen({super.key});

  @override
  State<PrivateDashboardScreen> createState() => _PrivateDashboardScreenState();
}

class _PrivateDashboardScreenState extends State<PrivateDashboardScreen> {
  final _storage = const FlutterSecureStorage();
  bool _hasDniVerified = false;
  final _dniController = TextEditingController();

  @override
  void dispose() {
    _dniController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Header
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 12, 12, 8),
              child: Row(
                children: [
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Mis Registros',
                          style: TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          'Sincronizado con ia-service + iot-service',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.white54,
                          ),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    onPressed: () async {
                      try {
                        await _storage.delete(key: 'auth_token');
                      } catch (_) {}
                      if (mounted) {
                        Navigator.pushAndRemoveUntil(
                          context,
                          MaterialPageRoute(
                            builder: (_) => const PublicParkingScreen(),
                          ),
                          (route) => false,
                        );
                      }
                    },
                    icon: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.08),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(
                        Icons.logout_rounded,
                        color: Colors.white70,
                        size: 20,
                      ),
                    ),
                  ),
                ],
              ),
            ),

            Expanded(
              child: !_hasDniVerified
                  ? _buildVerificationForm()
                  : _buildRecordsList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildVerificationForm() {
    return Padding(
      padding: const EdgeInsets.all(28),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: const Color(0xFFFFC107).withOpacity(0.12),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.badge_rounded,
              size: 40,
              color: Color(0xFFFFC107),
            ),
          ),
          const SizedBox(height: 24),
          const Text(
            'Verificación de Conductor',
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 12),
          const Text(
            'Ingresa tu DNI o Licencia de conducir\npara sincronizar tus parqueos en tiempo real.',
            textAlign: TextAlign.center,
            style: TextStyle(
              color: Colors.white54,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 32),
          TextField(
            controller: _dniController,
            keyboardType: TextInputType.number,
            style: const TextStyle(color: Colors.white),
            decoration: InputDecoration(
              labelText: 'Número de documento',
              labelStyle: const TextStyle(color: Colors.white54),
              prefixIcon: const Icon(Icons.pin, color: Colors.white54),
              filled: true,
              fillColor: const Color(0xFF16161C),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
                borderSide: BorderSide.none,
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
                borderSide: const BorderSide(
                  color: Color(0xFFFFC107),
                  width: 1.5,
                ),
              ),
            ),
          ),
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFFC107),
                foregroundColor: Colors.black,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
              ),
              onPressed: () {
                if (_dniController.text.trim().isNotEmpty) {
                  setState(() => _hasDniVerified = true);
                }
              },
              child: const Text(
                'Validar Documento',
                style: TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRecordsList() {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      children: [
        const Padding(
          padding: EdgeInsets.only(left: 4, bottom: 12),
          child: Text(
            'Vehículos asociados',
            style: TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w600,
              color: Colors.white70,
            ),
          ),
        ),

        // Registro en curso
        _buildRecordCard(
          plate: 'ABC-123',
          service: 'ai-service',
          location: 'Av. Luzuriaga C-3',
          time: 'Hoy • 16:15',
          isActive: true,
          amount: null,
        ),
        const SizedBox(height: 12),

        // Registro histórico
        _buildRecordCard(
          plate: 'ABC-123',
          service: 'iot-service',
          location: 'Jr. José de Sucre',
          time: '15/10/2026 • 18:00',
          isActive: false,
          amount: 'S/. 4.00',
        ),
      ],
    );
  }

  Widget _buildRecordCard({
    required String plate,
    required String service,
    required String location,
    required String time,
    required bool isActive,
    String? amount,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF16161C),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.06)),
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: isActive
                  ? Colors.blue.withOpacity(0.15)
                  : Colors.white.withOpacity(0.06),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              isActive ? Icons.camera_alt_rounded : Icons.history_rounded,
              color: isActive ? Colors.blueAccent : Colors.white54,
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      plate,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                        letterSpacing: 1.1,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 8,
                        vertical: 2,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.08),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        service,
                        style: const TextStyle(
                          fontSize: 10,
                          color: Colors.white54,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  location,
                  style: const TextStyle(
                    fontSize: 13,
                    color: Colors.white70,
                  ),
                ),
                Text(
                  time,
                  style: const TextStyle(
                    fontSize: 12,
                    color: Colors.white38,
                  ),
                ),
              ],
            ),
          ),
          if (isActive)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              decoration: BoxDecoration(
                color: Colors.blue.withOpacity(0.15),
                borderRadius: BorderRadius.circular(20),
              ),
              child: const Text(
                'En Curso',
                style: TextStyle(
                  color: Colors.blueAccent,
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                ),
              ),
            )
          else if (amount != null)
            Text(
              amount,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: Colors.greenAccent,
                fontSize: 15,
              ),
            ),
        ],
      ),
    );
  }
}