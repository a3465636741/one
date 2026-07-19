INSERT INTO organizers (id, name, description) VALUES
    (1, '启航科技社', '面向校园技术爱好者的示例组织'),
    (2, '光影摄影社', '校园摄影与视觉交流示例组织'),
    (3, '学生社团服务中心', '校园社团服务示例组织'),
    (4, '微光志愿服务队', '校园公益服务示例组织'),
    (5, '生涯发展工作坊', '职业发展活动示例组织'),
    (6, '纸页读书会', '校园阅读交流示例组织'),
    (7, '跃动羽毛球社', '校园体育活动示例组织'),
    (8, '未来学习实验室', '学习方法分享示例组织'),
    (9, '自然观察小组', '校园自然观察示例组织'),
    (10, '校园创意市集组', '校园创意活动示例组织'),
    (11, '国际交流协会', '语言与文化交流示例组织'),
    (12, '回声音乐社', '校园音乐活动示例组织');

INSERT INTO categories (id, name, slug, sort_order) VALUES
    (1, '学术讲座', 'lectures', 10),
    (2, '文体艺术', 'arts', 20),
    (3, '社团活动', 'clubs', 30),
    (4, '志愿公益', 'volunteer', 40),
    (5, '就业发展', 'career', 50),
    (6, '读书交流', 'reading', 60),
    (7, '体育赛事', 'sports', 70),
    (8, '校园生活', 'campus-life', 80),
    (9, '语言交流', 'language', 90);

INSERT INTO events (
    id, title, summary, content, category_id, organizer_id,
    start_time, end_time, location, cover_url, source_url, status, demo
) VALUES
    (1, '“从代码到产品”校园技术分享会', '面向初学者的产品开发经验分享，包含项目选题、协作和作品展示。', '分享从项目构思到可演示产品的完整过程，所有信息均为模拟内容。', 1, 1, '2026-09-08 19:00:00', '2026-09-08 20:30:00', '示例校园创新楼 201', 'https://picsum.photos/seed/campus-code/960/640', NULL, 'PUBLISHED', TRUE),
    (2, '秋日校园摄影打卡挑战', '围绕校园秋景完成主题拍摄并投稿，优秀作品将在校园展板展示。', '参与者可自由选择校园地点完成主题拍摄，所有信息均为模拟内容。', 2, 2, '2026-09-12 09:00:00', '2026-09-20 18:00:00', '示例校园全校范围', 'https://picsum.photos/seed/campus-photo/960/640', NULL, 'PUBLISHED', TRUE),
    (3, '新生校园定向与社团体验日', '通过定向任务了解校园服务点，并与多个学生社团面对面交流。', '在校园内完成定向任务并体验不同社团，所有信息均为模拟内容。', 3, 3, '2026-09-15 14:00:00', '2026-09-15 17:30:00', '示例校园风雨操场', 'https://picsum.photos/seed/campus-club/960/640', NULL, 'PUBLISHED', TRUE),
    (4, '旧物焕新公益捐赠行动', '收集干净可使用的书籍和生活用品，分类整理后定向捐赠。', '现场接收符合要求的物品并进行分类整理，所有信息均为模拟内容。', 4, 4, '2026-09-18 10:00:00', '2026-09-18 16:00:00', '示例校园图书馆广场', 'https://picsum.photos/seed/campus-volunteer/960/640', NULL, 'PUBLISHED', TRUE),
    (5, '简历优化与秋招求职训练营', '讲解简历结构、项目表达和模拟面试常见问题，适合有求职准备的学生。', '包含简历讲解和模拟交流环节，所有信息均为模拟内容。', 5, 5, '2026-09-22 19:00:00', '2026-09-22 21:00:00', '示例校园求真楼 108', 'https://picsum.photos/seed/campus-career/960/640', NULL, 'PUBLISHED', TRUE),
    (6, '城市记忆主题读书会', '围绕城市与个人成长主题进行共读和讨论，无需提前阅读完毕。', '参与者可携带自己喜欢的书籍参加讨论，所有信息均为模拟内容。', 6, 6, '2026-09-25 19:30:00', '2026-09-25 21:00:00', '示例校园图书馆研讨室 A', 'https://picsum.photos/seed/campus-reading/960/640', NULL, 'PUBLISHED', TRUE),
    (7, '校园羽毛球友谊赛', '双打友谊赛与新手体验场同步进行，欢迎不同水平的同学参与。', '比赛采用友谊交流赛制，所有信息均为模拟内容。', 7, 7, '2026-09-27 08:30:00', '2026-09-27 17:00:00', '示例校园综合体育馆', 'https://picsum.photos/seed/campus-sport/960/640', NULL, 'PUBLISHED', TRUE),
    (8, 'AI 工具与学习效率公开课', '介绍常见 AI 工具在资料整理、写作辅助和学习计划中的合理使用方式。', '课程强调合理使用工具和学术规范，所有信息均为模拟内容。', 1, 8, '2026-10-08 19:00:00', '2026-10-08 20:30:00', '示例校园学术报告厅', 'https://picsum.photos/seed/campus-ai/960/640', NULL, 'PUBLISHED', TRUE),
    (9, '校园植物观察与自然笔记体验', '在校园步道进行植物观察，学习记录自然笔记的基础方法。', '请根据天气准备合适衣物，所有信息均为模拟内容。', 4, 9, '2026-10-11 09:30:00', '2026-10-11 11:30:00', '示例校园北门集合', 'https://picsum.photos/seed/campus-nature/960/640', NULL, 'PUBLISHED', TRUE),
    (10, '创意市集摊主招募计划', '面向手作、绘画和旧物改造创作者开放摊位报名。', '活动提供模拟摊位申请入口，所有信息均为模拟内容。', 8, 10, '2026-10-16 12:00:00', '2026-10-18 18:00:00', '示例校园生活区广场', 'https://picsum.photos/seed/campus-market/960/640', NULL, 'PUBLISHED', TRUE),
    (11, '英语角：旅行与文化交流夜', '以旅行经历和文化差异为主题进行轻松英语交流。', '不同英语水平的参与者均可加入，所有信息均为模拟内容。', 9, 11, '2026-10-20 19:00:00', '2026-10-20 20:30:00', '示例校园外语楼 302', 'https://picsum.photos/seed/campus-language/960/640', NULL, 'PUBLISHED', TRUE),
    (12, '校园乐队开放舞台夜', '学生乐队和个人表演者可报名参与开放舞台，观众可自由到场观看。', '节目和入场规则以正式活动通知为准，所有信息均为模拟内容。', 2, 12, '2026-10-24 18:30:00', '2026-10-24 21:30:00', '示例校园大学生活动中心', 'https://picsum.photos/seed/campus-music/960/640', NULL, 'PUBLISHED', TRUE);

INSERT INTO event_entries (event_id, type, label, url, qr_image_url, sort_order) VALUES
    (1, 'LINK', '前往报名入口', 'https://example.com/demo-event/1', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F1', 10),
    (2, 'LINK', '查看活动规则', 'https://example.com/demo-event/2', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F2', 10),
    (3, 'LINK', '前往活动入口', 'https://example.com/demo-event/3', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F3', 10),
    (4, 'LINK', '查看捐赠说明', 'https://example.com/demo-event/4', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F4', 10),
    (5, 'LINK', '预约席位', 'https://example.com/demo-event/5', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F5', 10),
    (6, 'LINK', '加入读书会', 'https://example.com/demo-event/6', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F6', 10),
    (7, 'LINK', '查看赛程', 'https://example.com/demo-event/7', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F7', 10),
    (8, 'LINK', '前往活动页面', 'https://example.com/demo-event/8', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F8', 10),
    (9, 'LINK', '查看集合信息', 'https://example.com/demo-event/9', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F9', 10),
    (10, 'LINK', '查看招募入口', 'https://example.com/demo-event/10', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F10', 10),
    (11, 'LINK', '前往报名入口', 'https://example.com/demo-event/11', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F11', 10),
    (12, 'LINK', '了解演出安排', 'https://example.com/demo-event/12', 'https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=https%3A%2F%2Fexample.com%2Fdemo-event%2F12', 10);
