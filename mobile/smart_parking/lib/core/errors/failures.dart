/// Errores de la app, independientes de Dio y de HTTP.
/// Al ser `sealed`, Dart obliga a cubrir todos los casos en un `switch`.
sealed class Failure implements Exception {
  const Failure(this.message);
  final String message;
}

/// Sin conexión, tiempo de espera agotado o servidor inalcanzable.
final class NetworkFailure extends Failure {
  const NetworkFailure()
      : super('Sin conexión o tiempo de espera agotado');
}

/// El servidor respondió con un error (4xx / 5xx).
final class ServerFailure extends Failure {
  const ServerFailure(this.statusCode)
      : super('El servidor no pudo procesar la solicitud');
  final int? statusCode;
}

/// Cualquier error no previsto.
final class UnexpectedFailure extends Failure {
  const UnexpectedFailure() : super('Ocurrió un error inesperado');
}