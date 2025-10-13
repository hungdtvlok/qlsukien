package com.example.qlsukien;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Field;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public interface ApiService {

    // Đăng ký tài khoản
    @POST("/register")
    Call<ApiResponse> registerUser(@Body NhanVien nhanVien);

    // Lấy danh sách nhân viên
    @GET("/api/nhanvien")
    Call<List<NhanVien>> getNhanVienList();

    // Cập nhật thông tin nhân viên (fullName, email, phone)
    @PUT("/api/updateNhanVien")
    Call<ApiResponse> updateNhanVien(@Body NhanVien nhanVien);

    // Đổi mật khẩu nhân viên
    @FormUrlEncoded
    @PUT("/api/changePassword")
    Call<ApiResponse> changePassword(
            @Field("username") String username,
            @Field("newPassword") String newPassword
    );

    // Cập nhật avatar nhân viên (upload ảnh)
    @Multipart
    @POST("/api/updateAvatar")
    Call<ApiResponse> updateAvatar(
            @Part("username") RequestBody username,
            @Part MultipartBody.Part avatarFile
    );
}
