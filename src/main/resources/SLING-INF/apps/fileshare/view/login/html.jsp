<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling" %>



<sling:include path="/apps/fileshare/clientlibs/bootstrap/bootstrap.txt/jcr:content/jcr:data" />



<div class="container">
    <div class="row">
        <div class="col-sm-6 col-md-4 col-md-offset-4">
            <h1 class="text-center login-title">Sign in to continue to Bootsnipp</h1>
            <div class="account-wall">
                <img class="profile-img" src="https://lh5.googleusercontent.com/-b0-k99FZlyE/AAAAAAAAAAI/AAAAAAAAAAA/eu7opA4byxI/photo.jpg?sz=120"
                     alt="">
                <form method="POST" action="/j_security_check" autocomplete="off" class="form-signin">
                    <input type="text" class="form-control" placeholder="Username" required autofocus name="j_username">
                    <input type="password" class="form-control" placeholder="Password" required name="j_password">
                    <input type="hidden" name="sling.auth.redirect" value="/content/page/home.html">
                    <button class="btn btn-lg btn-primary btn-block" type="submit">
                        Sign in</button>
                    <label class="checkbox pull-left">
                        <input type="checkbox" value="remember-me">
                        Remember me
                    </label>
                    <a href="#" class="pull-right need-help">Need help? </a><span class="clearfix"></span>
                </form>
            </div>
            <a href="#" class="text-center new-account">Create an account </a>
        </div>
    </div>
</div>


<sling:include path="/content/fileshare/footer"  />

