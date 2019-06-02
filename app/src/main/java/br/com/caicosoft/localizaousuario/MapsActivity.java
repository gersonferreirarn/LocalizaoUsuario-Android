package br.com.caicosoft.localizaousuario;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION // permissao que será utilizada
    };
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Valida Permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        pegarEndereco(); // pega endereço por cordenadas e pega cordenadas por endereço

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // objeto responsável por gerencia a localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); // recupera serviço do sistema

        // escuta mudanças de localização
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { // quando a localização do usuario muda
                Log.i("TESTE", location.toString()); // LOCALIZAÇÃO DO USUARIO

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                marcaLocal(new LatLng(latitude, longitude));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { // quando o usuario ativa ou desativa a permissao

            }

            @Override
            public void onProviderEnabled(String provider) { // quando o usuario habilita a localização

            }

            @Override
            public void onProviderDisabled(String provider) { // quando o usuario desabilita a localização
                alertaValidacaoPermissao();
            }
        };

        solicitaAtualizacaoLocal(); // quando o usuario ja tiver aceito a permissao uma vez

    }

    public void pegarEndereco(){
        /*
        Geocoding -> processo de transformar um endereço ou descrição de um local em latitude/longitude
        Reverse Geocoding -> processo de transformar latitude/longitude em um endereço
        */
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault()); // usa localização padrao do usuario, para que o geocode saiba a localização para tratar os endereços
        try {

            // long e lat pra endereço
            //List<Address> listaEndereco = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1); // so vou querer um endereco

            // endereco pra lont e lat
            String stringEndereco = "Rua Anselmo Sebastião de Medeiros, 85, Caicó, RN";
            List<Address> listaEndereco = geocoder.getFromLocationName(stringEndereco, 1); // so vou querer um endereco

            if(listaEndereco != null && listaEndereco.size() > 0){ // caso exista algum endereço
                Address endereco = listaEndereco.get(listaEndereco.size()-1); // pega posição zero

                Log.i("ENDERECO", endereco.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void marcaLocal(LatLng latLng){
        mMap.clear(); // limpa o mapa
        mMap.addMarker(new MarkerOptions().position(latLng).title("Meu Local"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) { // percorre as permissoes


            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {// Permissão Denied (NEGADA)
                //Alerta
                alertaValidacaoPermissao();
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {// Permissão Granted (CONCEDIDA)

                solicitaAtualizacaoLocal(); // pede a permissao a primeira vez

            }

        }
    }

    public void solicitaAtualizacaoLocal(){
        // testa permissao especifica, se foi concedida
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Recuperar Localização do Usuário
            /*
             * 1) Provedor da Localização
             * 2) Tempo Minimo entre atualizações de localização (milesegundos)
             * 3) Distancia Minima entre atualizações de Localização (metros)
             * 4) Localion listener (para recebermos as atualizações)
             */

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, // todas as atualizações
                    0, // a qualquer movimento
                    locationListener

            ); // solicita atualizações de localização

            return;
        }
    }

    public void alertaValidacaoPermissao(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Permissões Negadas");
        dialog.setMessage("Para utilizar o app é necessário aceitar as permissões e habilitar a localização");

        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        //configura cancelamento
        dialog.setCancelable(false); // false: nao consegue fechar a dialog sem clicar em uma das opções

        //criar e exibir
        dialog.create();
        dialog.show();
    }
}