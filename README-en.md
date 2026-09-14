<div align="center">
<img src="./image-removebg-preview.png" alt="logo">
<h2 align="center">SlothNote - 树懒笔记</h2>
  <p>
  <a href="./README-cn.md">简体中文</a>  |  English
  </p>
<div>
<p>SlothNote is a cloud note platform based on Vue 3 and Spring Boot.</p>
</div>

<div>
  <img src="https://img.shields.io/badge/Project-Sloth-brightgreen"  alt="1"/>
  <img src="https://img.shields.io/badge/License-Apache2.0-blue"  alt="1"/>
  <img src="https://img.shields.io/badge/Version-1.0.0-orange"  alt="1"/>
</div>

</div>

## Features

- **Note Editing**: Supports online note editing with a rich text editor. Users can format text, insert images, tables, task lists, and more.
- **Collaborative Writing**: Supports real-time collaboration, allowing multiple users to edit the same note simultaneously. Changes are synchronized in real-time.
- **Mind Mapping (Tree Structure)**: Provides a tree-structured way to manage notes, helping users record and organize thoughts in a hierarchical format, like a mind map.
- **Task Reminders**: Offers to-do lists and task reminders. Users can set reminder times, with system notifications or pop-ups to alert them.
- **AI Assistance**: Integrates AI assistants to help users with content generation, grammar checking, smart recommendations, and more to enhance productivity.
- **Dark Mode**: Provides a dark mode to reduce eye strain during nighttime use and offer a more comfortable interface experience.
- **Admin Panel**: Features an admin panel where administrators can manage users, view logs, configure system settings, and adjust tasks.
- **Environment Variables (Config)**: Supports custom management of environment variables and configuration files, allowing users to easily adjust application settings.
- **Multi-Platform Support**: Can be deployed via Docker, supporting various operating systems.

## Development Tasks

- [x] Optimize table addition functionality
- [x] Add comment functionality
- [x] Add note presentation mode
- [ ] Add bookmark feature
- [ ] Add import/export feature
- [ ] Add sharing feature (with local saving)
- [ ] Add homepage functionality
- [ ] Add search functionality
- [ ] Test and optimize details
- [ ] Refactor code logic for optimization

## Deployment

The backend uses SQLite by default; MySQL is not required. On first start it automatically creates the database and safe system defaults:

- Default database location: `file/base/slothnote.db`
- Upload locations: `file/avatar/` and `file/noteImage/`
- Set `STORAGE_LOCAL_ROOT_DIR` to change the storage root, or `SQLITE_DB_PATH` to provide an absolute or working-directory-relative database file.

Create the first administrator through the Admin initialization screen or `POST /admin/auth/init` after the service starts. No administrator credentials or example business data are seeded.

Start the backend locally:

```bash
cd backend
mvn spring-boot:run
```

SQLite persists in a single database file. Back up `file/base/slothnote.db` while the service is stopped, retaining its adjacent `-wal` and `-shm` files when they exist. Do not publish `file/base/` as static content or commit it to source control.

For containers, mount `/app/file` to persist both SQLite data and uploads:

```bash
docker build -t slothnote-backend ./backend
docker run --rm -p 8080:8080 -v "$(pwd)/file:/app/file" slothnote-backend
```

## Technologies Used

- Backend: Java 17 + SpringBoot + SQLite + JPA + MyBatis-Plus + Sa-token + Swagger
- Frontend: Vue 3 + TypeScript + Axios + Router + Element Plus + TipTap

## Links

- [Tiptap Official Website](https://tiptap.dev)
- [Element Plus Official Website](https://element-plus.org)
- [Sa-Token Official Website](https://sa-token.cc/index.html)

Thank you for using SlothNote! 😊 If you have any questions or suggestions, feel free to contact us.
