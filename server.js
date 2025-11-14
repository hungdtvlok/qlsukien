
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
// công việc
const taskSchema = new mongoose.Schema({
  name: { type: String, required: true },
  startTime: { type: Date, required: true },
  endTime: { type: Date, required: true },
  performer: { type: String, default: "Chưa phân công" }
});
//chi tiêu
const expenseSchema = new mongoose.Schema({
  name: { type: String, required: true },
  money: { type: Number, required: true },
  createdAt: { type: Date, default: Date.now }
});
//chat
const chatSchema = new mongoose.Schema({
  sender: { type: String, required: true },   // "participant" hoặc "organizer"
  message: { type: String, required: true },  // nội dung tin nhắn
  username: { type: String, required: true }, // tên người gửi
  eventId: { type: String, required: true },  // ID sự kiện
  createdAt: { type: Date, default: Date.now }
});



const eventSchema = new mongoose.Schema({
    name: { type: String, required: true },
    startTime: { type: Date, required: true },
    endTime: { type: Date, required: true },
    location: { type: String, required: true },
    description: { type: String },
    createdAt: { type: Date, default: Date.now },
    maxParticipants: { type: Number, default: 100 }, // số lượng tối đa
    registeredCount: { type: Number, default: 0 },    // số lượng đã đăng ký
    tasks: [taskSchema] ,
    expenses: [expenseSchema],
    chats: [chatSchema]
});

const Event = mongoose.model("Event", eventSchema);
const User = mongoose.model("User", userSchema);

const Chat = mongoose.model("Chat", chatSchema)

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
            return res.status(400).json({ message: "Tài đăng nhập hoặc email đã tồn tại" });
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
        // Lấy danh sách sự kiện
        const events = await Event.find().sort({ createdAt: -1 });

        // Duyệt qua từng sự kiện để đếm số người đã đăng ký
        const eventsWithCounts = await Promise.all(events.map(async (event) => {
            const registeredCount = await Registration.countDocuments({ eventId: event._id }); // đếm số người đăng ký
            return {
                ...event.toObject(),
                registeredCount,                         // số người đăng ký
                totalSlots: event.totalSlots || 100       // tổng số chỗ (nếu chưa có trường này thì bạn để mặc định 100)
            };
        }));

        // Trả về kết quả
        res.json({
            message: "Lấy danh sách sự kiện thành công",
            count: eventsWithCounts.length,
            events: eventsWithCounts
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
        const { name, startTime, endTime, location, description, maxParticipants } = req.body;

        const event = await Event.findByIdAndUpdate(
            req.params.id,
            {
                name,
                startTime: startTime ? new Date(startTime) : undefined,
                endTime: endTime ? new Date(endTime) : undefined,
                location,
                description,
                maxParticipants
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


// =================== Công việc ===================






// Lấy tên sự kiện + ngày bắt đầu + ngày kết thúc
app.get("/api/eventnames", async (req, res) => {
    try {
        // Lấy các trường: name, startTime, endTime
        const events = await Event.find({}, "name startTime endTime");
        res.json(events);
    } catch (err) {
        console.error("❌ Lỗi khi lấy thông tin sự kiện:", err);
        res.status(500).json({ message: err.message });
    }
});

// Lấy danh sách công việc của 1 sự kiện cụ thể
app.get("/api/event/:eventId/tasks", async (req, res) => {
  try {
    const { eventId } = req.params;
    const event = await Event.findById(eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện" });

    const tasks = event.tasks.map(task => ({
      _id: task._id,
      name: task.name,
      startTime: task.startTime,
      endTime: task.endTime,
      performer: task.performer || "Chưa phân công"
    }));

    res.json({
      message: "Lấy danh sách công việc thành công",
      count: tasks.length,
      tasks
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Lỗi khi lấy công việc của sự kiện" });
  }
});


// POST thêm công việc
app.post("/api/event/:eventId/tasks", async (req, res) => {
  try {
    const { name, startTime, endTime, performer } = req.body;

    const event = await Event.findById(req.params.eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện" });

    // Chuyển sang Date
    const newTask = {
      name,
      startTime: startTime ? new Date(startTime) : null,
      endTime: endTime ? new Date(endTime) : null,
      performer: performer || "Chưa phân công"
    };

    // Kiểm tra dữ liệu bắt buộc
    if (!newTask.name || !newTask.startTime || !newTask.endTime) {
      return res.status(400).json({ message: "Thiếu thông tin bắt buộc của công việc" });
    }

    // Push vào array tasks
    event.tasks.push(newTask);
    await event.save();

    // Lấy task vừa thêm (có _id do Mongoose tự sinh)
    const addedTask = event.tasks[event.tasks.length - 1];

    res.status(201).json({ 
      message: "Thêm công việc thành công", 
      task: addedTask 
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: err.message });
  }
});



// PUT sửa công việc theo taskId
app.put("/api/event/:eventId/tasks/:taskId", async (req, res) => {
  try {
    const { name, startTime, endTime, performer } = req.body;
    const event = await Event.findById(req.params.eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện" });

    const task = event.tasks.id(req.params.taskId);
    if (!task) return res.status(404).json({ message: "Không tìm thấy công việc" });

    task.name = name || task.name;
    task.startTime = startTime || task.startTime;
    task.endTime = endTime || task.endTime;
    task.performer = performer || task.performer;

    await event.save();
    res.json({ message: "Cập nhật công việc thành công", task });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// DELETE công việc theo taskId
app.delete("/api/event/:eventId/tasks/:taskId", async (req, res) => {
  const { eventId, taskId } = req.params;
  console.log("DELETE taskId:", taskId, "eventId:", eventId);

  try {
    const event = await Event.findById(eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện" });

    // Debug: show tất cả task _id
    console.log("Tasks hiện tại:");
    event.tasks.forEach(t => console.log(t._id.toString()));

    // Xóa task đúng kiểu
    const taskIndex = event.tasks.findIndex(t => t._id.toString() === taskId.toString());
    if (taskIndex === -1) {
      console.log("Không tìm thấy task với _id:", taskId);
      return res.status(404).json({ message: "Không tìm thấy công việc" });
    }

    event.tasks.splice(taskIndex, 1); // xóa
    await event.save();

    console.log("Xóa task thành công:", taskId);
    res.json({ message: "Xóa công việc thành công", tasks: event.tasks });
  } catch (err) {
    console.error("Lỗi khi xóa task:", err);
    res.status(500).json({ message: err.message });
  }
});
// =================== CHI TIÊU ===================






// ================== API LẤY DANH SÁCH CHI TIÊU ==================
app.get("/api/event/:eventId/expenses", async (req, res) => {
  try {
    const event = await Event.findById(req.params.eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện!" });
    res.json({ expenses: event.expenses });
  } catch (error) {
    res.status(500).json({ message: "Lỗi server", error });
  }
});

// ================== API THÊM CHI TIÊU ==================

  app.post("/api/event/:eventId/expenses", async (req, res) => {
  try {
    const { name, money } = req.body;
    const event = await Event.findById(req.params.eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện!" });

    // Tạo khoản chi tiêu mới
    const newExpense = { name, money };
    event.expenses.push(newExpense);
    await event.save();

    // 🔹 Lấy lại phần tử vừa thêm (có _id do Mongo tạo)
    const addedExpense = event.expenses[event.expenses.length - 1];

    // 🔹 Trả về đúng dữ liệu client cần
    res.json({ message: "Thêm thành công", expense: addedExpense });
  } catch (error) {
    console.error("Lỗi khi thêm chi tiêu:", error);
    res.status(500).json({ message: "Lỗi khi thêm chi tiêu", error });
  }
});


// ================== API CẬP NHẬT CHI TIÊU ==================
app.put("/api/event/:eventId/expenses/:expenseId", async (req, res) => {
  try {
    const { name, money } = req.body;
    const event = await Event.findById(req.params.eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện!" });

    const expense = event.expenses.id(req.params.expenseId);
    if (!expense) return res.status(404).json({ message: "Không tìm thấy khoản chi tiêu!" });

    expense.name = name;
    expense.money = money;
    await event.save();

    res.json({ message: "Cập nhật thành công", expense });
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi cập nhật chi tiêu", error });
  }
});

// ================== API XÓA CHI TIÊU ==================
app.delete("/api/event/:eventId/expenses/:expenseId", async (req, res) => {
  try {
    const event = await Event.findById(req.params.eventId);
    if (!event) return res.status(404).json({ message: "Không tìm thấy sự kiện!" });

    event.expenses.id(req.params.expenseId).deleteOne();
    await event.save();

    res.json({ message: "Xóa thành công" });
  } catch (error) {
    res.status(500).json({ message: "Lỗi khi xóa chi tiêu", error });
  }
});

// ================= chat =================




// tải tin nhắn lên
app.post("/api/chat", async (req, res) => {
    try {
        const { sender, message, username, eventId } = req.body; // vẫn message
        if (!sender || !message || !username || !eventId) {
            return res.status(400).json({ message: "Thiếu thông tin chat" });
        }
        const chat = new Chat({ 
            sender, 
            message,   
            username, 
            eventId, 
            timestamp: new Date().toISOString() 
        });
        await chat.save();
        res.json({ message: "Đã gửi", chat });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});


// lấy tin nhắn
app.get("/api/chat/:eventId", async (req, res) => {
    try {
        const { eventId } = req.params;
        console.log("LẤY CHAT CHO EVENTID:", eventId);

        const chats = await Chat.find({ eventId }).sort({ createdAt: 1 });

        console.log("CHAT TÌM ĐƯỢC:", chats.length);
        res.json(chats);
    } catch (err) {
        console.error("LỖI LẤY CHAT:", err.message, err);
        res.status(500).json({ message: err.message });
    }
});

const multer = require('multer');

const path = require("path");

// Nơi lưu ảnh
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, "uploads/chat"); // thư mục uploads/chat/
    },
    filename: (req, file, cb) => {
        cb(null, Date.now() + path.extname(file.originalname)); 
    }
});

const upload = multer({ storage: storage });

// API upload ảnh
app.post("/api/chat/upload", upload.single("image"), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ message: "Không có ảnh được tải lên" });
    }

    const imageUrl = `/uploads/chat/${req.file.filename}`;
    
    res.json({
        message: "Upload thành công",
        imageUrl: imageUrl
    });
});

// ================== REGISTRATION SCHEMA ==================
const registrationSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
    eventId: { type: mongoose.Schema.Types.ObjectId, ref: "Event", required: true },
    registeredAt: { type: Date, default: Date.now },
    status: { type: String, default: "pending" }, //"pending" hoặc "joined"
    emailSent: { type: Boolean, default: false }
});

const Registration = mongoose.model("Registration", registrationSchema);


// Đăng ký tham gia sự kiện
app.post("/api/registerEvent", async (req, res) => {
    try {
        const { username, eventId } = req.body;

        if (!username || !eventId) {
            return res.status(400).json({ message: "Thiếu username hoặc eventId" });
        }

        // 🔹 Lấy user & event
        const user = await User.findOne({ username });
        const event = await Event.findById(eventId);

        if (!user || !event) {
            return res.status(404).json({ message: "Không tìm thấy người dùng hoặc sự kiện" });
        }

        // 🔹 Kiểm tra đã đăng ký chưa
        const existing = await Registration.findOne({ userId: user._id, eventId });
        if (existing) {
            return res.status(400).json({ message: "❗ Bạn đã đăng ký sự kiện này rồi" });
        }

        // 🔹 Đếm số người đã đăng ký hiện tại (luôn chính xác)
        const currentCount = await Registration.countDocuments({ eventId });

        // 🔹 Kiểm tra vượt giới hạn
        if (currentCount >= event.maxParticipants) {
            return res.status(400).json({
                message: ` Sự kiện đã đủ ${event.maxParticipants} người tham gia`
            });
        }

        // 🔹 Lưu đăng ký mới
        const registration = new Registration({
            userId: user._id,
            eventId: event._id
        });
        await registration.save();

        // 🔹 Cập nhật lại số người đã đăng ký trong Event (để hiển thị nhanh ở Android)
        event.registeredCount = currentCount + 1;
        await event.save();

        res.json({
            message: " Đăng ký sự kiện thành công",
            registration,
            event: {
                name: event.name,
                location: event.location,
                startTime: event.startTime,
                endTime: event.endTime,
                registeredCount: event.registeredCount,
                maxParticipants: event.maxParticipants
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
        //  Giảm số người đã đăng ký
        if (eventExists.registeredCount > 0) {
            eventExists.registeredCount -= 1;
            await eventExists.save();
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
    registeredBy: { type: String, default: "người đăng ký" },
    status: { type: String, default: "pending" }
});

const Participant = mongoose.model("Participant", participantSchema);


// ================== API THÊM PARTICIPANT VÀ TỰ ĐỘNG TẠO REGISTRATION ==================
app.post("/api/addParticipant", async (req, res) => {
    try {
        const { fullName, email, phone, eventName, startTime, endTime, status } = req.body;

        if (!fullName || !email || !phone || !eventName) {
            return res.status(400).json({ message: "Thiếu thông tin participant" });
        }

        const existingParticipant = await Participant.findOne({ email, eventName });
        if (existingParticipant) {
            return res.status(400).json({ message: "Đã tồn tại" });
        }

        const newParticipant = new Participant({
            fullName,
            email,
            phone,
            eventName,
            startTime: startTime ? new Date(startTime) : null,
            endTime: endTime ? new Date(endTime) : null,
            status: status || "pending",
            registeredBy: "người đăng ký"
        });
        await newParticipant.save();
        console.log("✅ Participant đã tạo:", newParticipant);

        const user = await User.findOne({ fullName, email, phone });
        const event = await Event.findOne({ name: eventName });

        // Chỉ cập nhật status nếu tìm thấy user và event, không tạo thêm
        if (user && event) {
            const existingRegistration = await Registration.findOne({ userId: user._id, eventId: event._id });
            if (existingRegistration) {
                existingRegistration.status = status || "joined";
                await existingRegistration.save();
                console.log("✅ Cập nhật status Registration:", existingRegistration.status);
            } else {
                console.log("⚠️ Registration chưa tồn tại → không tạo thêm");
            }
        } else {
            console.log("⚠️ Không tìm thấy user hoặc event → không tạo Registration");
        }

        // Trả về participant thành công mà không cần registration
        res.status(201).json({
            message: "✅ Thêm participant thành công",
            participant: newParticipant,
            userId: user ? user._id : null,
            eventId: event ? event._id : null
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
        const { fullName, email, phone, eventName, startTime, endTime, registeredBy } = req.body;

        if (!fullName || !email || !phone || !eventName || !registeredBy) {
            return res.status(400).json({ message: "Thiếu thông tin participant" });
        }

        // 🔎 Kiểm tra trùng (dựa vào email + eventName hoặc phone + eventName)
        const existing = await Participant.findOne({ email, eventName });
        if (existing) {
            return res.status(400).json({ message: "❌ Participant đã có trong danh sách" });
        }

        const newParticipant = new Participant({
            fullName,
            email,
            phone,
            eventName,
            startTime: startTime ? new Date(startTime) : null,
            endTime: endTime ? new Date(endTime) : null,
            registeredBy
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
        const { fullName, email, phone, eventName, startTime, endTime, registeredBy } = req.body;

        const updateFields = {};

        if (fullName !== undefined) updateFields.fullName = fullName;
        if (email !== undefined) updateFields.email = email;
        if (phone !== undefined) updateFields.phone = phone;
        if (eventName !== undefined) updateFields.eventName = eventName;
        if (startTime !== undefined) updateFields.startTime = new Date(startTime);
        if (endTime !== undefined) updateFields.endTime = new Date(endTime);
        if (registeredBy !== undefined) updateFields.registeredBy = registeredBy;

        const updatedParticipant = await Participant.findByIdAndUpdate(
            id,
            { $set: updateFields }, // chỉ set những field cần update
            { new: true }
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




// ================== API THỐNG KÊ NGƯỜI THAM GIA + CHI TIÊU ==================

app.get("/api/statistics", async (req, res) => {
  try {
    //  Đếm số người tham gia theo sự kiện (từ bảng Participant)
    const participants = await Participant.aggregate([
      { 
        $group: { 
          _id: "$eventName", 
          count: { $sum: 1 } 
        } 
      }
    ]);

    //  Lấy danh sách tất cả sự kiện và tính tổng chi tiêu
    const events = await Event.find();

    //  Gộp dữ liệu lại (participants + expenses)
    const merged = events.map(ev => {
      const p = participants.find(x => x._id === ev.name);
      const count = p ? p.count : 0;

      // Tính tổng chi tiêu của sự kiện (dựa trên mảng expenses)
      const totalExpense = ev.expenses?.reduce((sum, e) => sum + (e.money || 0), 0) || 0;

      return {
        eventName: ev.name,
        count,
        totalExpense
      };
    });

    //  Thêm sự kiện có người tham gia nhưng chưa tồn tại trong Event (tránh bị sót)
    participants.forEach(p => {
      if (!merged.find(m => m.eventName === p._id)) {
        merged.push({
          eventName: p._id,
          count: p.count,
          totalExpense: 0
        });
      }
    });

    //  Sắp xếp theo tên sự kiện
    merged.sort((a, b) => a.eventName.localeCompare(b.eventName));

    //  Trả kết quả về client
    res.json({
      message: "✅ Lấy thống kê thành công",
      statistics: merged
    });

  } catch (err) {
    console.error("❌ Lỗi khi thống kê:", err);
    res.status(500).json({ message: err.message });
  }
});




// ================== API QUÊN MẬT KHẨU ==================
const sgMail = require("@sendgrid/mail");
const crypto = require("crypto");

sgMail.setApiKey(process.env.SENDGRID_API_KEY);


// ================== API QUÊN MẬT KHẨU ==================
app.post("/api/quenmk", async (req, res) => {
  try {
    const { username } = req.body;
    if (!username)
      return res.status(400).json({ message: "Thiếu tên tài khoản!" });

    // Tìm user theo username (không phân biệt hoa thường)
    const user = await User.findOne({
      username: { $regex: `^${username.trim()}$`, $options: "i" },
    });
    if (!user)
      return res.status(404).json({ message: "Không tìm thấy tài khoản!" });
    if (!user.email)
      return res.status(400).json({ message: "Tài khoản chưa có email!" });

    // 🔐 Tạo mật khẩu tạm thời
    const tempPassword = Math.floor(100000 + Math.random() * 900000).toString();

    // Mã hoá mật khẩu
    const hashedPassword = await bcrypt.hash(tempPassword, 10);
    user.password = hashedPassword;

    // ✅ Lưu thật sự vào MongoDB
    await user.save();

    // Gửi email và chỉ trả phản hồi khi gửi xong
    const msg = {
      to: user.email,
      from: "githich462@gmail.com", // email đã verify
      subject: "Khôi phục mật khẩu - QLSK",
      html: `
        <p>Xin chào <b>${user.username}</b>,</p>
        <p>Mật khẩu tạm thời của bạn là: <b>${tempPassword}</b></p>
        <p>Hãy đăng nhập và đổi mật khẩu ngay.</p>
      `,
    };

    // ⏳ Đợi gửi mail xong mới phản hồi
    await sgMail.send(msg);
    console.log(`📧 Email gửi thành công tới ${user.email}`);

    // ✅ Trả kết quả sau khi cả hai bước hoàn tất
    res.json({ message: "Mật khẩu tạm thời đã được gửi tới email của bạn!" });
  } catch (err) {
    console.error("❌ Lỗi API quên mật khẩu:", err);
    res.status(500).json({ message: "Lỗi server: " + err.message });
  }
});
// ================== Gửi email nhắc nhở trước 2h bằng SendGrid ==================
const cron = require("node-cron");

const { DateTime } = require("luxon");

sgMail.setApiKey(process.env.SENDGRID_API_KEY);

// ================== Route kiểm tra server / ping ==================
app.get("/api/ping", (req, res) => {
    res.json({ message: "Server alive" });
});

// ================== Hàm gửi email ==================
async function sendEmail(reg, startTimeVN) {
    const formattedTime = startTimeVN.toFormat("dd/MM/yyyy 'lúc' HH:mm");

    const msg = {
        to: reg.userId.email,
        from: "githich462@gmail.com", // email đã verify SendGrid
        subject: `📢 Nhắc nhở: ${reg.eventId.name} sắp bắt đầu!`,
        text: `Xin chào ${reg.userId.fullName},

Sự kiện "${reg.eventId.name}" sẽ bắt đầu lúc ${formattedTime}.
Địa điểm: ${reg.eventId.location || "chưa cập nhật"}.

Hẹn gặp bạn tại sự kiện!

Trân trọng,
Ban tổ chức.`,
        html: `<p>Xin chào <b>${reg.userId.fullName}</b>,</p>
               <p>Sự kiện "<b>${reg.eventId.name}</b>" sẽ bắt đầu lúc <b>${formattedTime}</b>.</p>
               <p>Địa điểm: ${reg.eventId.location || "chưa cập nhật"}.</p>
               <p>Hẹn gặp bạn tại sự kiện!</p>
               <p>Trân trọng,<br/>Ban tổ chức.</p>`
    };

    try {
        await sgMail.send(msg);
        console.log(`✅ Đã gửi email đến ${reg.userId.email}`);
        reg.emailSent = true;
        await reg.save();
    } catch (err) {
        console.error(`❌ Gửi email lỗi cho ${reg.userId.email}:`, err.response ? err.response.body : err);
    }
}

// ================== Route gửi nhắc nhở ==================
app.post("/api/send-reminder", async (req, res) => {
    const nowVN = DateTime.now().setZone("Asia/Ho_Chi_Minh");
    const twoHoursLaterVN = nowVN.plus({ hours: 2 });

    try {
        const registrations = await Registration.find();

        for (const reg of registrations) {
            if (!reg.eventId || !reg.userId || reg.emailSent) continue;

            const startTimeVN = DateTime.fromJSDate(reg.eventId.startTime).setZone("Asia/Ho_Chi_Minh");

            if (startTimeVN > nowVN && startTimeVN <= twoHoursLaterVN) {
                await sendEmail(reg, startTimeVN);
            }
        }

        res.json({ message: "Nhắc nhở email đã gửi (nếu có sự kiện sắp diễn ra)" });
    } catch (err) {
        console.error("❌ Lỗi gửi nhắc nhở:", err);
        res.status(500).json({ message: "Lỗi server" });
    }
});








/*
================== Gửi Gmail thật trước 2 h==================

const cron = require("node-cron");
const nodemailer = require("nodemailer");
const { DateTime } = require("luxon");

// ===== Cấu hình Gmail SMTP =====
const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 465,
    secure: true,
    auth: {
        user: "githich462@gmail.com",
        pass: "aqzzbtyfarsgaesd", // App Password
    },
});

transporter.verify((error, success) => {
    if (error) console.error("❌ Lỗi cấu hình Gmail:", error);
    else console.log("✅ Gmail SMTP sẵn sàng để gửi email!");
});

// ===== Hàm gửi email =====
async function sendEmail(reg, startTimeVN) {
    const formattedTime = startTimeVN.toFormat("dd/MM/yyyy 'lúc' HH:mm");

    const mailOptions = {
        from: '"Ban tổ chức sự kiện" <githich462@gmail.com>',
        to: reg.userId.email,
        subject: `📢 Nhắc nhở: ${reg.eventId.name} sắp bắt đầu!`,
        text: `Xin chào ${reg.userId.fullName},

Sự kiện "${reg.eventId.name}" sẽ bắt đầu lúc ${formattedTime}.
Địa điểm: ${reg.eventId.location || "chưa cập nhật"}.

Hẹn gặp bạn tại sự kiện!

Trân trọng,
Ban tổ chức.`
    };

    try {
        await transporter.sendMail(mailOptions);
        console.log(`✅ Đã gửi email đến ${reg.userId.email}`);
        reg.emailSent = true;
        await reg.save();
    } catch (err) {
        console.error(`❌ Gửi email lỗi cho ${reg.userId.email}:`, err);
    }
}

// ===== Cron job: kiểm tra mỗi phút =====
cron.schedule("* * * * *", async () => {
    console.log("🔍 Kiểm tra sự kiện sắp bắt đầu...");
    ...
});
*/





// ================== START SERVER ==================
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`✅ Server running on http://localhost:${PORT}`);
});









































































































































