package com.android.app.imagen;

import android.content.Context;
import android.provider.Settings;

public class Talkback {

    protected Context context;

    public Talkback(Context context) {
        this.context = context;
    }
    public void enableTalkback() {
        // Comprobamos si la aplicación tiene permiso WRITE_SECURE_SETTINGS
        if (context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == context.getPackageManager().PERMISSION_GRANTED) {
            // Se Activa el TalkBack cambiando la configuración del sistema
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService");
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");
        } else {
            // Si la aplicación no tiene permiso, lanzar una excepción o manejar el error de otra manera
            throw new SecurityException("La aplicación no tiene permiso WRITE_SECURE_SETTINGS");
        }
    }
    public void disableTalkback() {
        if (context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == context.getPackageManager().PERMISSION_GRANTED) {
            // Desactivar TalkBack cambiando la configuración del sistema
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "");
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "0");
        } else {
            // Si la aplicación no tiene permiso, lanzar una excepción o manejar el error de otra manera
            throw new SecurityException("La aplicación no tiene permiso WRITE_SECURE_SETTINGS");
        }
    }
}
