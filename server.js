// ================== IMPORT MODULES ==================
const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

// ================== INIT APP ==================
const app = express();

// Middleware để parse JSON từ client
app.use(bodyParser.json());

// Cho phép CORS để Android app có thể gọi API
app.use(cors());

// 🔑 SECRET KEY cho JWT
const JWT_SECRET = "secret123";

// ================== MONGO DB CONNECTION ==================
mongoose.connect(
    "mongodb+srv://bdx:123456789%40@cluster0.xmmbfgf.mongodb.net/qlsukien?retryWrites=true&w=majority&appName=Cluster0",
    { useNewUrlParser: true, useUnifiedTopology: true }
)
.then(() => console.log("✅ MongoDB connected"))
.catch(err => console.error("❌ MongoDB connection error:", err));

// ================== USER SCHEMA ==================
const userSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    fullName: { type: String },
    email: { type: String, required: true, unique: true },
    phone: { type: String, required: true },
    password: { type: String, required: true },
    avatar: { type: String, default: "" }, // thêm avatar
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now },
    sole: { type: String, default: "User" }
});
const eventSchema = new mongoose.Schema({
    name: { type: String, required: true },
    startTime: { type: Date, required: true },
    endTime: { type: Date, required: true },
    location: { type: String, required: true },
    description: { type: String },
    createdAt: { type: Date, default: Date.now }
});

const Event = mongoose.model("Event", eventSchema);
const User = mongoose.model("User", userSchema);



// ================== API REGISTER ==================
app.post("/api/register", async (req, res) => {
    try {
        console.log("Body received:", req.body);
        const { username, fullName, email, phone, password, sole } = req.body; // lấy thêm sole từ client nếu có

        // Kiểm tra dữ liệu bắt buộc
        if (!username || !password || !email || !phone) {
            return res.status(400).json({ message: "Thông tin đăng ký không hợp lệ" });
        }

        // Kiểm tra trùng username/email
        const existingUser = await User.findOne({ $or: [{ username }, { email }] });
        if (existingUser) {
            const field = existingUser.username === username ? "Username" : "Email";
            return res.status(400).json({ message: "Tài khoản hoặc email đã tồn tại" });
        }

        // Hash password
        const hashedPassword = await bcrypt.hash(password, 10);

        // Tạo user mới với sole mặc định là "User" nếu client không gửi
        const newUser = new User({
            username,
            fullName,
            email,
            phone,
            password: hashedPassword,
            sole: sole || "User"
        });

        // Lưu vào MongoDB
        await newUser.save();

        res.status(201).json({ message: "Đăng ký thành công" });

    } catch (err) {
        console.error("❌ Lỗi đăng ký:", err);

        if (err.code === 11000) {
            const field = Object.keys(err.keyValue)[0];
            return res.status(400).json({ message: `${field} đã tồn tại` });
        }

        res.status(500).json({ message: "Server error: " + err.message });
    }
});



// ================== API LOGIN ==================
app.post("/api/login", async (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({ message: "Thông tin đăng nhập không hợp lệ" });
        }

        // Tìm user theo username
        const user = await User.findOne({ username });
        if (!user) {
            return res.status(400).json({ message: "Sai tài khoản hoặc mật khẩu" });
        }

        // Kiểm tra password
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ message: "Sai tài khoản hoặc mật khẩu" });
        }

        // Tạo JWT token (1 giờ)
        const token = jwt.sign({ id: user._id, username: user.username }, JWT_SECRET, { expiresIn: "1h" });

        // Trả về token
        res.json({ 
    message: "Đăng nhập thành công", 
    token, 
    role: user.sole.toLowerCase() 
});

    } catch (err) {
        console.error("❌ Lỗi đăng nhập:", err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});
// ================== API LẤY NHÂN VIÊN ==================
app.get("/api/nhanvien", async (req, res) => {
    try {
        const { username } = req.query; // Lấy username từ query param
        let users;

        if (username) {
            // Nếu có username, chỉ trả về user đó
            users = await User.find({ username });
        } else {
            // Nếu không có username, trả về tất cả user
            users = await User.find();
        }

        res.json(users);
    } catch (err) {
        console.error("❌ Lỗi lấy nhân viên:", err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});

// ================== API SỬA THÔNG TIN NHÂN VIÊN ==================
app.post("/api/updateNhanVien", async (req, res) => {
    try {
        const { username, fullName, email, phone } = req.body;

        if (!username || !fullName || !email || !phone) {
            return res.status(400).json({ message: "Thiếu thông tin cập nhật" });
        }

        const user = await User.findOne({ username });
        if (!user) return res.status(404).json({ message: "Người dùng không tồn tại" });

        // Kiểm tra email trùng (nếu đổi)
        const emailExists = await User.findOne({ email, username: { $ne: username } });
        if (emailExists) return res.status(400).json({ message: "Email đã tồn tại" });

        user.fullName = fullName;
        user.email = email;
        user.phone = phone;
        user.updatedAt = new Date();

        await user.save();
        res.json({ message: "Cập nhật thông tin thành công" });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});

// ================== API ĐỔI MẬT KHẨU ==================
app.post("/api/changePassword", async (req, res) => {
    try {
        const { username, newPassword } = req.body;

        if (!username || !newPassword) return res.status(400).json({ message: "Thiếu thông tin đổi mật khẩu" });

        const user = await User.findOne({ username });
        if (!user) return res.status(404).json({ message: "Người dùng không tồn tại" });

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        user.password = hashedPassword;
        user.updatedAt = new Date();

        await user.save();
        res.json({ message: "Đổi mật khẩu thành công" });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});
// ================== ảnh ==================
app.post("/api/updateAvatar", async (req, res) => {
    try {
        console.log("Body nhận từ client:", req.body);

        const { username, avatar } = req.body;
        if (!username || !avatar) {
            return res.status(400).json({ message: "Thiếu thông tin avatar" });
        }

        const user = await User.findOne({ username });
        if (!user) {
            return res.status(404).json({ message: "Người dùng không tồn tại" });
        }

        console.log("Avatar nhận từ client (50 ký tự đầu):", avatar.substring(0, 50));

        user.avatar = avatar;
        user.updatedAt = new Date();
        await user.save();

        console.log("User sau khi cập nhật:", user);

        res.json({ message: "Cập nhật avatar thành công", avatar: user.avatar });
    } catch (err) {
        console.error("Lỗi trong updateAvatar:", err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});

// ================= sự kiện =================


// Lấy tất cả sự kiện
app.get("/api/events", async (req, res) => {
    try {
        const events = await Event.find().sort({ createdAt: -1 });

        res.json({
            message: "Lấy danh sách sự kiện thành công",
            count: events.length,
            events
        });
    } catch (err) {
        console.error("❌ Lỗi khi lấy sự kiện:", err);
        res.status(500).json({ message: err.message });
    }
});

// Tạo sự kiện (có startTime và endTime)
app.post("/api/events", async (req, res) => {
    try {
        const { name, startTime, endTime, location, description } = req.body;

        // Kiểm tra dữ liệu đầu vào
        if (!name || !startTime || !endTime || !location) {
            return res.status(400).json({ message: "Thiếu thông tin sự kiện" });
        }

        // Tạo mới sự kiện
        const event = new Event({
            name,
            startTime: new Date(startTime), // chuyển về kiểu Date
            endTime: new Date(endTime),
            location,
            description
        });

        await event.save();

        res.json({
            message: "✅ Tạo sự kiện thành công",
            event
        });
    } catch (err) {
        console.error("❌ Lỗi tạo sự kiện:", err);
        res.status(500).json({ message: err.message });
    }
});


// Sửa sự kiện
app.put("/api/events/:id", async (req, res) => {
    try {
        const { name, startTime, endTime, location, description } = req.body;

        // Chuyển ISO string sang Date
        const event = await Event.findByIdAndUpdate(
            req.params.id,
            {
                name,
                startTime: startTime ? new Date(startTime) : undefined,
                endTime: endTime ? new Date(endTime) : undefined,
                location,
                description
            },
            { new: true } // trả về document mới cập nhật
        );

        if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện" });

        res.json({ message: "Cập nhật sự kiện thành công", event });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});


// Xóa sự kiện
app.delete("/api/events/:id", async (req, res) => {
    try {
        await Event.findByIdAndDelete(req.params.id);
        res.json({ message: "Xóa sự kiện thành công" });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// ================== REGISTRATION SCHEMA ==================
const registrationSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    eventId: { type: mongoose.Schema.Types.ObjectId, ref: "Event", required: true },
    registeredAt: { type: Date, default: Date.now }
});

const Registration = mongoose.model("Registration", registrationSchema);

// Đăng ký tham gia sự kiện
app.post("/api/registerEvent", async (req, res) => {
    try {
        const { username, eventId } = req.body;

        if (!username || !eventId) {
            return res.status(400).json({ message: "Thiếu username hoặc eventId" });
        }

        // Tìm user theo username
        const user = await User.findOne({ username });
        const event = await Event.findById(eventId);

        if (!user || !event) {
            return res.status(404).json({ message: "User hoặc Event không tồn tại" });
        }

        // Kiểm tra đã đăng ký chưa
        const existing = await Registration.findOne({ userId: user._id, eventId });
        if (existing) {
            return res.status(400).json({ message: "Bạn đã đăng ký sự kiện này rồi" });
        }

        // Tạo đăng ký mới
        const registration = new Registration({ userId: user._id, eventId });
        await registration.save();

        res.json({
            message: "Đăng ký sự kiện thành công",
            registration,
            user: {
                username: user.username,
                fullName: user.fullName,
                email: user.email,
                phone: user.phone
            },
            event: {
                name: event.name,
                location: event.location,
                startTime: event.startTime,
                endTime: event.endTime
            }
        });
    } catch (err) {
        console.error("❌ Lỗi đăng ký sự kiện:", err);
        res.status(500).json({ message: err.message });
    }
});

// Lấy tất cả sự kiện đã đăng ký bởi tất cả user
app.get("/api/allregisterEvent", async (req, res) => {
    try {
        // Lấy tất cả registrations và populate userId + eventId
        const registrations = await Registration.find()
            .populate("userId", "username fullName email phone")  // Thông tin user
            .populate("eventId", "name location startTime endTime description"); // Thông tin sự kiện

        res.json({
            message: "Lấy tất cả sự kiện đã đăng ký thành công",
            count: registrations.length,
            registrations
        });

    } catch (err) {
        console.error("❌ Lỗi khi lấy sự kiện đã đăng ký:", err);
        res.status(500).json({ message: err.message });
    }
});


 // Lấy sự kiện mà user đã đăng ký theo username
app.get("/api/registerEvent/:username", async (req, res) => {
    try {
        const { username } = req.params;

        const user = await User.findOne({ username });
        if (!user) return res.status(404).json({ message: "User không tồn tại" });

        // Lấy tất cả registrations và populate userId + eventId
        const registrations = await Registration.find({ userId: user._id })
            .populate("userId", "username fullName email phone")  // Chỉ lấy 4 trường
            .populate("eventId", "name location startTime endTime"); // Chỉ lấy 4 trường

        res.json({
            message: "Lấy danh sách registrations thành công",
            registrations
        });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

//  chỉnh sửa đăng ký sự kiện
app.post("/api/EditRegisterEvent", async (req, res) => {
    try {
        const { registrationId, userId, eventId } = req.body;

        if (!registrationId) {
            return res.status(400).json({ success: false, message: "registrationId là bắt buộc" });
        }

        const registration = await Registration.findById(registrationId);
        if (!registration) {
            return res.status(404).json({ success: false, message: "Không tìm thấy đăng ký" });
        }

        if (userId) registration.userId = mongoose.Types.ObjectId(userId);
        if (eventId) registration.eventId = mongoose.Types.ObjectId(eventId);

        await registration.save();

        return res.json({
            success: true,
            message: "Chỉnh sửa đăng ký thành công",
            registration
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, message: "Lỗi server" });
    }
});

// Hủy đăng ký sự kiện
app.post("/api/unregisterEvent", async (req, res) => {
    try {
        const { userId, eventId } = req.body;

        if (!userId || !eventId) {
            return res.status(400).json({ message: "Thiếu userId hoặc eventId" });
        }

        // Kiểm tra user và event tồn tại
        const userExists = await User.findById(userId);
        const eventExists = await Event.findById(eventId);

        if (!userExists || !eventExists) {
            return res.status(404).json({ message: "User hoặc Event không tồn tại" });
        }

        // Xóa tất cả đăng ký trùng userId + eventId
        const result = await Registration.deleteMany({
            userId: new mongoose.Types.ObjectId(userId),
            eventId: new mongoose.Types.ObjectId(eventId)
        });

        if (result.deletedCount === 0) {
            return res.status(404).json({ message: "Không tìm thấy đăng ký để hủy" });
        }

        res.json({ message: `Đã hủy ${result.deletedCount} đăng ký thành công` });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: err.message });
    }
});

// ================== PARTICIPANT SCHEMA ==================
const participantSchema = new mongoose.Schema({
    fullName: { type: String },
    email: { type: String },
    phone: { type: String },
    eventName: { type: String },
    startTime: { type: Date },
    endTime: { type: Date },
    createdAt: { type: Date, default: Date.now }, 
    registeredBy: { type: String, default: "người đăng ký" } 
});

const Participant = mongoose.model("Participant", participantSchema);

// ================== API thêm PARTICIPANT ==================
app.post("/api/addParticipant", async (req, res) => {
    try {
        const { fullName, email, phone, eventName, startTime, endTime } = req.body;

        if (!fullName || !email || !phone || !eventName) {
            return res.status(400).json({ message: "Thiếu thông tin participant" });
        }

        const newParticipant = new Participant({
            fullName,
            email,
            phone,
            eventName,
            startTime: startTime ? new Date(startTime) : null,
            endTime: endTime ? new Date(endTime) : null,
            registeredBy: "" // luôn mặc định
        });

        await newParticipant.save();

        res.status(201).json({
            message: "Thêm participant thành công",
            participant: newParticipant
        });
    } catch (err) {
        console.error("❌ Lỗi khi thêm participant:", err);
        res.status(500).json({ message: err.message });
    }
});

// Lấy tất cả participants
app.get("/api/participants", async (req, res) => {
    try {
        const participants = await Participant.find().sort({ createdAt: -1 });
        res.json({
            message: "Lấy danh sách participants thành công",
            count: participants.length,
            participants
        });
    } catch (err) {
        console.error("❌ Lỗi khi lấy participants:", err);
        res.status(500).json({ message: err.message });
    }
});
// ================== API THÊM PARTICIPANT ==================
app.post("/api/participants", async (req, res) => {
    try {
        const { fullName, email, phone, eventName, startTime, endTime } = req.body;

        if (!fullName || !email || !phone || !eventName) {
            return res.status(400).json({ message: "Thiếu thông tin participant" });
        }

        const newParticipant = new Participant({
            fullName,
            email,
            phone,
            eventName,
            startTime: startTime ? new Date(startTime) : null,
            endTime: endTime ? new Date(endTime) : null,
            registeredBy: "người đăng ký"
        });

        await newParticipant.save();

        res.status(201).json({
            message: "✅ Thêm participant thành công",
            participant: newParticipant
        });
    } catch (err) {
        console.error("❌ Lỗi khi thêm participant:", err);
        res.status(500).json({ message: err.message });
    }
});
// ================== API SỬA PARTICIPANT ==================
app.put("/api/participants/:id", async (req, res) => {
    try {
        const { id } = req.params;
        const { fullName, email, phone, eventName, startTime, endTime } = req.body;

        const updatedParticipant = await Participant.findByIdAndUpdate(
            id,
            {
                fullName,
                email,
                phone,
                eventName,
                startTime: startTime ? new Date(startTime) : null,
                endTime: endTime ? new Date(endTime) : null,
            },
            { new: true } // trả về bản ghi sau khi update
        );

        if (!updatedParticipant) {
            return res.status(404).json({ message: "❌ Không tìm thấy participant để sửa" });
        }

        res.json({
            message: "✅ Cập nhật participant thành công",
            participant: updatedParticipant
        });
    } catch (err) {
        console.error("❌ Lỗi khi cập nhật participant:", err);
        res.status(500).json({ message: err.message });
    }
});
// ================== API XÓA PARTICIPANT ==================
app.delete("/api/participants/:id", async (req, res) => {
    try {
        const { id } = req.params;
        const deletedParticipant = await Participant.findByIdAndDelete(id);

        if (!deletedParticipant) {
            return res.status(404).json({ message: "❌ Không tìm thấy participant để xóa" });
        }

        res.json({
            message: "✅ Xóa participant thành công",
            participant: deletedParticipant
        });
    } catch (err) {
        console.error("❌ Lỗi khi xóa participant:", err);
        res.status(500).json({ message: err.message });
    }
});






// ================== START SERVER ==================
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`✅ Server running on http://localhost:${PORT}`);
});













































