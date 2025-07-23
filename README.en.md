# ch-sso

#### Description
单点登录

#### Software Architecture
ch-sso is an enterprise-level single sign-on system built with Spring Boot + Spring Security. The core architecture includes:
- **Authentication & Authorization**: Based on OAuth2.0 protocol, supporting password and authorization code grant types
- **Token Management**: Uses JWT for Access Token and Refresh Token generation, with Redis cache for token storage and validation
- **Service Governance**: Integrates Alibaba Nacos as service registry and configuration center
- **Security Protection**: Includes captcha (AWT generated), slide verification, and password encryption storage
- **Cluster Support**: Supports distributed deployment with clusterized OAuth2 configuration (manual activation required)

Architecture Flowchart:
```
User → Business System → SSO Authentication Center → Token Generation → Business System Token Validation → Resource Access
```

#### Installation
```shell
mvn -U -pl client -am clean deploy -Dmaven.test.skip -Drevision=2.1.0-SNAPSHOT

```

1. xxxx
2. xxxx
3. xxxx

#### Instructions

### Core API Interfaces

#### Authentication
- `GET /login` - Get login page
- `POST /login/access` - User login (parameters: username, password, captcha)
- `GET /login/auth-code` - Get mobile verification code
- `GET /login/token/refresh` - Refresh token
- `GET /login/captcha` - Get image captcha
- `GET /login/slideCaptcha` - Get slide verification

#### User Management
- `GET /user` - Get user list
- `GET /user/info` - Get current user information
- `POST /user/permissions` - Get user permissions
- `GET /user/auth-code` - Get user authorization code

#### Token Operations
- `GET /api/token/validate` - Validate token
- `GET /api/token/user-info` - Get token associated user info
- `GET /api/token/refresh-token` - Get refresh token

#### Logout
- `GET /oauth/logout` - User logout
- `POST /logout/token` - Invalidate token

### Usage Examples
**User Login**:
```bash
curl -X POST http://localhost:7000/login/access \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456","captcha":"8a3d"}'
```

**Validate Token**:
```bash
curl -X GET http://localhost:7000/api/token/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Contribution

1. Fork the repository
2. Create Feat_xxx branch
3. Commit your code
4. Create Pull Request


#### Gitee Feature

1. You can use Readme\_XXX.md to support different languages, such as Readme\_en.md, Readme\_zh.md
2. Gitee blog [blog.gitee.com](https://blog.gitee.com)
3. Explore open source project [https://gitee.com/explore](https://gitee.com/explore)
4. The most valuable open source project [GVP](https://gitee.com/gvp)
5. The manual of Gitee [https://gitee.com/help](https://gitee.com/help)
6. The most popular members  [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)