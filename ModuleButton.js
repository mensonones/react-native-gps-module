import React from 'react';
import {Alert, Button} from 'react-native';
import MyGPSModule from './MyGPSModule/MyGPSModule.js';

const ModuleButton = () => {
  const onPress = () => {
    MyGPSModule.getCoordinatesByGPS()
      .then(res => {
        console.log('RESULT: ', res);
        Alert.alert(`LATITUDE: ${res.latitude}
LONGITUDE: ${res.longitude}`);
      })
      .catch(err => {
        console.log('ERROR: ', err);
      });
  };

  return (
    <Button
      title="Clique aqui para buscar coordenadas!"
      color="#841584"
      onPress={onPress}
    />
  );
};

export default ModuleButton;
