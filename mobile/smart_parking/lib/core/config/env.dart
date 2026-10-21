/// Configuración por entorno.
/// El valor se inyecta al ejecutar: --dart-define=API_BASE_URL=http://...
abstract final class Env {
  static const String apiBaseUrl = String.fromEnvironment('API_BASE_URL');

  /// Falla al arrancar si falta la URL del api-gateway,
  /// en lugar de fallar más tarde con un error confuso.
  static void validate() {
    if (apiBaseUrl.isEmpty) {
      throw StateError(
        'Falta --dart-define=API_BASE_URL=<url del api-gateway>',
      );
    }
  }
}