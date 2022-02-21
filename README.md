# React Native GPS Module

### React Native Module é um projeto para fins didáticos
Esse projeto é um módulo React Native, que visa obter as coordenadas do usuário a partir do GPS/Network

![enter image description here](https://i.ibb.co/FXGrZF1/Woman-with-medical-mask-holding-smartphone-and-gps-mark-on-map-design-of-Covid-19-virus-theme-Vector.jpg)

### Instalação
1. Clonar projeto
   - `git clone https://github.com/mensonones/react-native-gps-module.git`
2. Abrir o projeto em seu editor favorito e instalar as dependências
   - `yarn`
3. Executar o projeto 
   - `yarn start --reset-cache`
   - `npx react-native run-android`
   


#### Métodos
```javascript
// Abre uma caixa de diálogo para permissão, caso não haja.
// Abre uma caixa de diálogo para ir nas configurações e ativar o GPS, caso o mesmo esteja desativado
// Com permissões e GPS ativado, é retonado a coordenada
MyGPSModule.getCoordinatesByGPS()

// Exemplo do retorno

{"latitude": -3.7535016666666667, "longitude": -38.521946666666665}
```

```
### Suporte

- [x] Android
- [pendente] iOS
- [pedente] Web
```
