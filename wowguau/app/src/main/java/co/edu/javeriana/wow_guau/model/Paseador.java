package co.edu.javeriana.wow_guau.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Paseador extends Usuario {
    private String descripcion;
    private int experiencia;
    private List<String> direccionesCertificados;

    public Paseador(String correo, String nombre, int cedula, Date fechaNacimiento, int telefono, String genero, String direccion, String descripcion, int experiencia) {
        super(correo, nombre, cedula, fechaNacimiento, telefono, genero, direccion);
        this.descripcion = descripcion;
        this.experiencia = experiencia;
        this.direccionesCertificados = new ArrayList<>();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(int experiencia) {
        this.experiencia = experiencia;
    }

    public List<String> getDireccionesCertificados() {
        return direccionesCertificados;
    }

    public void setDireccionesCertificados(List<String> direccionesCertificados) {
        this.direccionesCertificados = direccionesCertificados;
    }
}
