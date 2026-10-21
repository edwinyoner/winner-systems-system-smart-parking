import 'package:dio/dio.dart';

import '../errors/failures.dart';

/// Convierte un error de Dio en un [Failure] propio.
Failure mapDioException(DioException e) {
  switch (e.type) {
    case DioExceptionType.connectionTimeout:
    case DioExceptionType.sendTimeout:
    case DioExceptionType.receiveTimeout:
    case DioExceptionType.connectionError:
      return const NetworkFailure();
    case DioExceptionType.badResponse:
      return ServerFailure(e.response?.statusCode);
    default:
      return const UnexpectedFailure();
  }
}