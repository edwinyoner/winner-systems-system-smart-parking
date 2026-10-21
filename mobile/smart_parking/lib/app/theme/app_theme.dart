import 'package:flutter/material.dart';

abstract final class AppTheme {
  /// Cambia el color semilla por el de tu marca cuando lo definas.
  static ThemeData get light => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
      );
}