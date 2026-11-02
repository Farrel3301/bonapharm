import java.io.Console;
import java.util.Arrays;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Utilidad local: solicita la clave sin mostrarla ni recibirla como argumento. */
class GenerarHash {
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Ejecuta esta utilidad en una terminal interactiva.");
        }
        char[] clave = console.readPassword("Contraseña del nuevo usuario: ");
        char[] confirmacion = console.readPassword("Repite la contraseña: ");
        try {
            if (clave == null || confirmacion == null || clave.length == 0 || !Arrays.equals(clave, confirmacion)) {
                throw new IllegalArgumentException("La contraseña no puede estar vacía y debe coincidir.");
            }
            System.out.println(new BCryptPasswordEncoder().encode(new String(clave)));
        } finally {
            if (clave != null) Arrays.fill(clave, '\0');
            if (confirmacion != null) Arrays.fill(confirmacion, '\0');
        }
    }
}
