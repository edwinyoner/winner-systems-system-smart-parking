import 'package:flutter/material.dart';

class ParkingListPage extends StatelessWidget {
  const ParkingListPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      appBar: AppBar(title: Text('Estacionamientos')),
      body: Center(child: Text('Base lista. Aquí irá el listado.')),
    );
  }
}