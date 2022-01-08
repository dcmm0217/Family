## Spring Security 身份认证流程

先来一段大家很熟悉的代码：

```java
http.formLogin()
    .loginPage("/auth/login")
    .permitAll()
    .failureHandler(loginFailureHandler)
    .successHandler(loginSuccessHandler);
```

Spring Security 就像一个害羞的大姑娘，就这么一段鬼知道他是怎么认证的，封装的有点过哈。不着急

根据JavaEE的流程，本质就是Filter过滤请求，转发到不同处理模块处理，最后经过业务逻辑处理，返回Response的过程。

当请求匹配了我们定义的Security Filter的时候，就会导向Security 模块进行处理，例如UsernamePasswordAuthenticationFilter，源码献上:

```java
public class UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private boolean postOnly = true;

    public UsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String username = this.obtainUsername(request);
            String password = this.obtainPassword(request);
            if (username == null) {
                username = "";
            }

            if (password == null) {
                password = "";
            }

            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setUsernameParameter(String usernameParameter) {
        Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
        this.usernameParameter = usernameParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
        this.passwordParameter = passwordParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getUsernameParameter() {
        return this.usernameParameter;
    }

    public final String getPasswordParameter() {
        return this.passwordParameter;
    }
}

```

有点复杂是吧，不用担心，我来做一些伪代码，让他看起来更友善，更好理解。注意我写的单行注释

```java
public class UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
    private String usernameParameter = "username";
    private String passwordParameter = "password";
    private boolean postOnly = true;

    public UsernamePasswordAuthenticationFilter() {
        //1.匹配URL和Method
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            //啥？你没有用POST方法，给你一个异常，自己反思去
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            //从请求中获取参数
            String username = this.obtainUsername(request);
            String password = this.obtainPassword(request);
            //我不知道用户名密码是不是对的，所以构造一个未认证的Token先
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            //顺便把请求和Token存起来
            this.setDetails(request, token);
            //Token给谁处理呢？当然是给当前的AuthenticationManager喽
            return this.getAuthenticationManager().authenticate(token);
        }
    }
}
```

是不是很清晰，问题又来了，Token是什么鬼？为啥还有已认证和未认证的区别？别着急，咱们顺藤摸瓜，来看看Token长啥样。上UsernamePasswordAuthenticationToken:

```java
public class UsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 510L;
    private final Object principal;
    private Object credentials;

    public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super((Collection)null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
    }

    public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    public Object getCredentials() {
        return this.credentials;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        } else {
            super.setAuthenticated(false);
        }
    }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
```

一坨坨的真闹心，我再备注一下：

```java
public class UsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 510L;
    //随便怎么理解吧，暂且理解为认证标识吧，没看到是一个Object么
    private final Object principal;
    //同上
    private Object credentials;

    //这个构造方法用来初始化一个没有认证的Token实例
    public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super((Collection)null);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(false);
    }
	//这个构造方法用来初始化一个已经认证的Token实例，为啥要多此一举，不能直接Set状态么，不着急，往后看
    public UsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }
	//便于理解无视他
    public Object getCredentials() {
        return this.credentials;
    }
	//便于理解无视他
    public Object getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            //如果是Set认证状态，就无情的给一个异常，意思是：
            //不要在这里设置已认证，不要在这里设置已认证，不要在这里设置已认证
            //应该从构造方法里创建，别忘了要带上用户信息和权限列表哦
            //原来如此，是避免犯错吧
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        } else {
            super.setAuthenticated(false);
        }
    }

    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }
}
```

搞清楚了Token是什么鬼，其实只是一个载体而已啦。接下来进入核心环节，AuthenticationManager是怎么处理的。这里我简单的过渡一下，但是会让你明白。

AuthenticationManager会注册多种AuthenticationProvider，例如UsernamePassword对应的DaoAuthenticationProvider，既然有多种选择，那怎么确定使用哪个Provider呢？我截取了一段源码，大家一看便知：

```java
public interface AuthenticationProvider {
    Authentication authenticate(Authentication var1) throws AuthenticationException;

    boolean supports(Class<?> var1);
}
```

这是一个接口，我喜欢接口，简洁明了。里面有一个supports方法，返回时一个boolean值，参数是一个Class，没错，这里就是根据Token的类来确定用什么Provider来处理，大家还记得前面的那段代码吗？

```java
//Token给谁处理呢？当然是给当前的AuthenticationManager喽
return this.getAuthenticationManager().authenticate(token);
```

因此我们进入下一步，DaoAuthenticationProvider，继承了AbstractUserDetailsAuthenticationProvider，恭喜您再坚持一会就到曙光啦。这个比较复杂，为了不让你跑掉，我将两个复杂的类合并，摘取直接触达接口核心的逻辑，直接上代码，会有所删减，让你看得更清楚，注意看注释：

```java
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    //熟悉的supports，需要UsernamePasswordAuthenticationToken
    public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        	//取出Token里保存的值
            String username = authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
            boolean cacheWasUsed = true;
        	//从缓存取
            UserDetails user = this.userCache.getUserFromCache(username);
            if (user == null) {
                cacheWasUsed = false;

                //啥，没缓存？使用retrieveUser方法获取呀
                user = this.retrieveUser(username, (UsernamePasswordAuthenticationToken)authentication);
            }
            //...删减了一大部分，这样更简洁
            Object principalToReturn = user;
            if (this.forcePrincipalAsString) {
                principalToReturn = user.getUsername();
            }

            return this.createSuccessAuthentication(principalToReturn, authentication, user);
        }
         protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        try {
            //熟悉的loadUserByUsername
            UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
            } else {
                return loadedUser;
            }
        } catch (UsernameNotFoundException var4) {
            this.mitigateAgainstTimingAttack(authentication);
            throw var4;
        } catch (InternalAuthenticationServiceException var5) {
            throw var5;
        } catch (Exception var6) {
            throw new InternalAuthenticationServiceException(var6.getMessage(), var6);
        }
    }
	//检验密码
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            this.logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        } else {
            String presentedPassword = authentication.getCredentials().toString();
            if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
                this.logger.debug("Authentication failed: password does not match stored value");
                throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }
        }
    }
}
```

到此为止，就完成了用户名密码的认证校验逻辑，根据认证用户的信息，系统做相应的Session持久化和Cookie回写操作。

Spring Security的基本认证流程先写到这里，其实复杂的背后是一些预定，熟悉了之后就不难了。

> Filter->构造Token->AuthenticationManager->转给Provider处理->认证处理成功后续操作或者不通过抛异常

总结：

1、AbstractAuthenticationProcessingFilter 的  doFilter 方法 进行请求过滤

2、BasicAuthenticationConverter  的 convert 方法 设置一个未认证的token

```java
UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(token.substring(0, delim), token.substring(delim + 1));
result.setDetails(this.authenticationDetailsSource.buildDetails(request));
return result;
```

3、AbstractUserDetailsAuthenticationProvider 的 authenticate()方法

4、DaoAuthenticationProvider的additionalAuthenticationChecks（）去检查密码是否正确