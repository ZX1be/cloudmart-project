import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public  class getBC{
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("admin123"));
    }
}