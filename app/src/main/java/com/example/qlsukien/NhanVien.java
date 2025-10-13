package com.example.qlsukien;

public class NhanVien {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String createdAt;
    private String updatedAt;
    private String avatar; // base64 string
    private String sole;   // thêm trường sole

    // Constructor đầy đủ
    public NhanVien(String username, String fullName, String email, String phone,
                    String password, String createdAt, String updatedAt, String avatar) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.avatar = avatar;
        this.sole = sole;
    }

    // Getter và Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getSole() { return sole; }
    public void setSole(String sole) { this.sole = sole; } // thêm getter/setter cho sole
}
