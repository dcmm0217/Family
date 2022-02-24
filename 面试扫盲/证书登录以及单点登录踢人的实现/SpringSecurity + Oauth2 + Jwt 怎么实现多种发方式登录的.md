## SpringSecurity + Oauth2 + Jwt 怎么实现多种发方式登录的？

1、根据前端的不同的登录方式`GRANT_TYPE`的差别，访问到后端的自定义鉴权器（`AbstractTokenGranter ` 的实现类）里面，自定义鉴权器会构造一个未认证的token，比如`UsernamePasswordAuthenticationToken`,

2、再通过`authenticationManager.authenticate()`方法去认证这个token

3、认证过程中又调了这个`ProviderManager.authentication()`去区分未认证token的一个类型

4、又调了`DaoAuthenticationProvider.authenticate()`，这个方法里面做了`additionalAuthenticationChecks()`密码匹配、``retrieveUser()``方法里面调了这个`UserDetailsService.loadUserByUsername()`进行身份查找，最后`createSuccessAuthentication()`认证完成往 `SecurityContext`中放一个认证过的 auth 对象

5、`UserDetailsService.loadUserByUsername()`类是用来加载用户信息，包括**用户名**、**密码**、**权限**等，简单来说就是登录用户跟数据库去匹配。如果账号密码对了，状态不对被锁定，或者说是被禁用了，在此处可以抛出异常。

6、如果`authenticationManager.authenticate()`认证无问题，返回一个`new OAuth2Authentication(oauthRequest, userAuth)`就是创建了一个`createAccessToken`存入`tokenStore`

7、然后我们在令牌增强器`TokenEhancer`里面 修改配置即可。隐藏关键信息，添加自己需要的信息

8、最后调用`TokenEndpoint`的一个`/oauth/token`接口 即可完成整个登录流程。

