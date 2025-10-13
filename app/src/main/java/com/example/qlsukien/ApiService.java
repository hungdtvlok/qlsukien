package com.example.qlsukien;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

import retrofit2.http.DELETE;
import retrofit2.http.Path;


public interface ApiService {

    // ====================== Đăng ký tài khoản ======================
    @POST("/api/register")
    Call<ApiResponse> registerUser(@Body NhanVien nhanVien);

    // ====================== Login ======================
    @FormUrlEncoded
    @POST("/api/login")
    Call<ApiResponse> loginUser(
            @Field("username") String username,
            @Field("password") String password
    );

    // ====================== Lấy danh sách nhân viên ======================
    @GET("/api/nhanvien")
    Call<List<NhanVien>> getNhanVienList();

    // ====================== Cập nhật thông tin nhân viên (fullName, email, phone) ======================
    @PUT("/api/updateNhanVien")
    Call<ApiResponse> updateNhanVien(@Body NhanVien nhanVien);

    // ====================== Đổi mật khẩu nhân viên ======================
    @FormUrlEncoded
    @PUT("/api/changePassword")
    Call<ApiResponse> changePassword(
            @Field("username") String username,
            @Field("newPassword") String newPassword
    );


    // ====================== Cập nhật avatar nhân viên (base64 JSON) ======================
    @POST("/api/updateAvatar")
    Call<ApiResponse> updateAvatar(@Body NhanVien nhanVien);

    // ====================== Lấy danh sách sự kiện ======================
    @GET("/api/events")
    Call<List<SuKien>> getSuKienList();

    // ====================== Thêm sự kiện ======================
    @POST("/api/events")
    Call<ApiResponse> addSuKien(@Body SuKien suKien);

    // ====================== Sửa sự kiện ======================
    @PUT("/api/events/{id}")
    Call<ApiResponse> updateSuKien(
            @retrofit2.http.Path("id") String id,
            @Body SuKien suKien
    );

    // ====================== Xóa sự kiện ======================
    @retrofit2.http.DELETE("/api/events/{id}")
    Call<ApiResponse> deleteSuKien(@retrofit2.http.Path("id") String id);





    // ====================== Đăng ký tham gia sự kiện ======================
    @POST("/api/registerEvent")
    Call<ApiResponse> registerEvent(@Body DangKySuKien request);

    // ====================== Lấy tất cả sự kiện đã đăng ký bởi tất cả user ======================
    @GET("/api/allregisterEvent")
    Call<List<SuKienDaDangKyAll>> getAllRegisteredEvents();

    // ====================== Hủy đăng ký sự kiện ======================
    @DELETE("/api/unregisterEvent")
    Call<ApiResponse> unregisterEvent(@Body DangKySuKien request);

    // ====================== Lấy danh sách sự kiện của user ======================
    @GET("/api/registerEvent/{userId}")
    Call<List<SuKien>> getMyEvents(@Path("userId") String userId);
}
