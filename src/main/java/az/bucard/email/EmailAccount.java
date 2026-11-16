package az.bucard.email;

public record EmailAccount(
        String address,
        String password,
        String token,
        String accountId
) {
    public boolean isValid() {
        return address != null && !address.isBlank() && password != null;
    }
}
