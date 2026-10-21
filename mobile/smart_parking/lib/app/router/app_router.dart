import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/parking/presentation/pages/parking_list_page.dart';

/// Todas las rutas actuales son públicas. Cuando exista el login se
/// agregará un `redirect` solo para las rutas privadas.
final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const ParkingListPage(),
      ),
    ],
  );
});