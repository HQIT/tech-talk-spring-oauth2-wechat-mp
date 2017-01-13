# tech-talk-spring-oauth2-wechat-mp
Example on integration of spring-cloud-security and WeChat MP's oauth
简单示例演示如何使用spring-cloud-security配置微信公众号平台的网页认证

## 说明
1. 因为微信公众号的oauth过程不复合标准, 比如```client_id```变成了```appid```等
2. 如果不想麻烦配置spring-oauth2, 可以使用```weixin-java-mp```自己处理跳转. 令需要自己处理spring-security相关的部分, 比如```principal```.
``` xml
<dependency>
  <groupId>com.github.binarywang</groupId>
  <artifactId>weixin-java-mp</artifactId>
  <version>${weixin.mp.version}</version>
</dependency>
```
3. 这个例子并不全面, 不一定适用所有的情境, 当前选用的```spring-boot```版本是
``` xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>1.4.2.RELEASE</version>
  <relativePath />
</parent>
```

## WeChat网页授权的问题
1. 用户同意授权获取code时```client_id```变成了```appid```
```
https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
```
2. 通过code换取网页授权access_token需要```appid```和```secret```参数,要求```HTTP GET```方法
```
https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
```
3. 拉取用户信息时需要```access_token```/```openid```和```lang```参数
```
https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN
```

### 问题1
```spring oauth2```的实现是参照oauth2标准的，因此代码中*hard code*了很多```client_id```字符串, 不能通过override类似```getClientIdParameterValue()```这样的形式来将```client_id```修改为```appid```

经过查看```spring```的相关代码, 发现是通过```org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter.redirectUser(UserRedirectRequiredException, HttpServletRequest, HttpServletResponse)```来进行authorize地址的拼装. 而```OAuth2ClientContextFilter```是由```org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration```配置并且创建的. 并且```OAuth2ClientConfiguration```是被```@EnableOAuth2Client```注解引入的.
``` java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OAuth2ClientConfiguration.class)
public @interface EnableOAuth2Client {

}
```
```EnableOAuth2Client```是被```EnableOAuth2Sso```引用的
``` java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableOAuth2Client
@EnableConfigurationProperties(OAuth2SsoProperties.class)
@Import({ 
  OAuth2SsoDefaultConfiguration.class, 
  OAuth2SsoCustomConfiguration.class,
  ResourceServerTokenServicesConfiguration.class })
public @interface EnableOAuth2Sso {

}
```

所以只需要实现自己的```OAuth2ClientConfiguration```在其中派生```OAuth2ClientContextFilter```并override其中的```redirectUser```方法增加```appid```参数就可以了
``` java
/**
 * 跳转到微信认证时需要appid参数用于携带client_id
 */
@Configuration
public class MyOAuth2ClientConfiguration extends OAuth2ClientConfiguration {

  static public class MyOAuth2ClientContextFilter extends OAuth2ClientContextFilter {
    @Override
    protected void redirectUser(
      UserRedirectRequiredException e, 
      HttpServletRequest request,
      HttpServletResponse response
      ) throws IOException {
        String clientId = e.getRequestParams().get("client_id");
        e.getRequestParams().put("appid", clientId);
        super.redirectUser(e, request, response);
    }
  }

  @Override
  public OAuth2ClientContextFilter oauth2ClientContextFilter() {
    return new MyOAuth2ClientContextFilter();
  }
}
```
*从```e.getRequestParams()```中remove掉```client_id```也可以, 实际发现微信```appid```和```client_id```同时存在不影响认证, 可以保留*

为了用我们自己的```MyOAuth2ClientConfiguration```因此不能直接用```@EnableOAuth2Sso```注解 **@SpringBootApplication** , 展开来自己写
``` java
/**
 * 这是UI服务器
 */
@SpringBootApplication
@EnableConfigurationProperties(OAuth2SsoProperties.class)
@Import({ 
  MyOAuth2ClientConfiguration.class, 
  OAuth2SsoDefaultConfiguration.class, 
  OAuth2SsoCustomConfiguration.class,
  ResourceServerTokenServicesConfiguration.class })
public class WxAppApplication {
  public static void main(String[] args){
    SpringApplication.run(WxAppApplication.class, args);
  }
}
```

### 问题2
```org.springframework.security.oauth2.client.token.OAuth2AccessTokenSupport.retrieveToken(AccessTokenRequest, OAuth2ProtectedResourceDetails, MultiValueMap<String, String>, HttpHeaders)```(```AuthorizationCodeAccessTokenProvider```派生于该类)中提供了通过```tokenRequestEnhancer```在想auth server请求```access token```前修改请求参数和请求头的机会, 而```AuthorizationCodeAccessTokenProvider```是在```OAuth2RestTemplate```中被调用的, 因此我们通过```UserInfoRestTemplateCustomizer```来设定```OAuth2RestTemplate```.

``` java
/**
 * 用于通过customize方法修改OAuth2RestTemplate中的AuthorizationCodeAccessTokenProvider,
 * 给AuthorizationCodeAccessTokenProvider设置新的TokenRequestEnhancer,
 * TokenRequestEnhancer中可以修改获取AccessToken时的uri参数
 */
@Configuration
@Component
public class MyUserInfoRestTemplateCustomizer implements UserInfoRestTemplateCustomizer {
  /**
   * 需要通过TokenRequestEnhancer设置appid
   */
  @Override
  public void customize(OAuth2RestTemplate template) {
    AuthorizationCodeAccessTokenProvider accessTokenProvider = new MyAuthorizationCodeAccessTokenProvider();
    accessTokenProvider.setTokenRequestEnhancer(new MyWxAccessTokenRequestEnhancer());
    template.setAccessTokenProvider(accessTokenProvider);
  }
}
```
其中```MyAuthorizationCodeAccessTokenProvider```和```MyWxAccessTokenRequestEnhancer```都是```MyUserInfoRestTemplateCustomizer```的内部类(不是内部类也没关系).

```
static class MyAuthorizationCodeAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
  /**
   * 微信用GET方式, spring oauth2框架只在GET时将form中参数拼接到url中
   */
  @Override
  protected HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  /**
   * 微信的response body是json格式的
   */
  @Override
  protected ResponseExtractor<OAuth2AccessToken> getResponseExtractor() {
    getRestTemplate(); // force initialization
    return new HttpMessageConverterExtractor<OAuth2AccessToken>(
      OAuth2AccessToken.class, 
      Arrays.asList(new WxOAuth2AccessTokenMessageConverter()));
  }
}
```
其中override ```getHttpMethod```使通过**GET**方法获取access token, 默认是**POST**方法并且不会将参数拼接到请求url中.
其中spring oauth默认把response body当成form来解析, 造成失败, 因此需要自定义消息转换器```WxOAuth2AccessTokenMessageConverter```(实现看源码, 很简单).

### 问题3

```spring oauth```的默认实现中, 获取用户详情的方法没有带任何参数, 直接就是```application.properties```中的配置值.

经过分析发现```org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices.loadAuthentication(String)```的```org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices.getMap(String, String)```有机会修改```userInfoEndpointUrl```, 因此需要配置自己的```UserInfoTokenServices```(spring发现创建了就不自动创建这个bean)
```
@Configuration
static protected class MyUserInfoTokenServicesConfiguration {
  //自动装配需要用到的beans
}
```
其中内嵌类```MyUserInfoTokenServices```覆盖```loadAuthentication```方法拼接带参数的```userInfoEndpointUrl```. 注意其中的```openid```在上一步拿到的```access token```的```additionalInformation```中.

``` java
class MyUserInfoTokenServices extends UserInfoTokenServices {
  //...

  @Override
  public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
    //...
    /**
     * 如果希望在应用中@RequestMapping装配Principal需要手动设置, 因为没有检查openid这个属性名
     * 如果想用自己的账号体系, 可以在这个位置访问自己的用户服务获取用户详情
     */
  }
  //...
}
```
*更多详情请看源代码*

## 关于@Configuration注解

``` java
@Configuration
static class XXXConfiguration {
  XXXConfiguration(
    ExistBean bean
  ){
  }
}
```

**@Configuration**注解的类框架会自动调用构造函数(例子中是```XXXConfiguration```)并且自动装配构造参数(例子中是```ExistBean```)