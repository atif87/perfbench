package grocketjpa.wicket

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import javax.persistence.Persistence
import org.apache.wicket.Component
import org.apache.wicket.IConverterLocator
import org.apache.wicket.Request
import org.apache.wicket.RequestCycle
import org.apache.wicket.Response
import org.apache.wicket.RestartResponseException
import org.apache.wicket.authorization.Action
import org.apache.wicket.authorization.IAuthorizationStrategy
import org.apache.wicket.protocol.http.HttpSessionStore
import org.apache.wicket.protocol.http.WebApplication
import org.apache.wicket.protocol.http.WebRequest
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebRequestProcessor
import org.apache.wicket.request.IRequestCycleProcessor
import org.apache.wicket.session.ISessionStore
import org.apache.wicket.util.convert.ConverterLocator
import org.apache.wicket.util.convert.converters.BigDecimalConverter

class BookingApplication extends WebApplication {

    def entityManagerFactory
    
    def Class getHomePage() {
        MainPage.class
    }
    
    def void init() {
        super.init()
        entityManagerFactory = Persistence.createEntityManagerFactory("bookingDatabase")
        securitySettings.authorizationStrategy = new IAuthorizationStrategy() {            
            def boolean isActionAuthorized(Component c, Action a) {
                true
            }            
            def boolean isInstantiationAuthorized(Class clazz) {
                if (TemplatePage.class.isAssignableFrom(clazz)) {
                    if (BookingSession.get().user == null) {
                        throw new RestartResponseException(HomePage.class)
                    }
                }
                true
            }
        }
        markupSettings.compressWhitespace = true
        mountBookmarkablePage("/home", HomePage.class)
        mountBookmarkablePage("/logout", LogoutPage.class)
        mountBookmarkablePage("/register", RegisterPage.class)
        mountBookmarkablePage("/settings", PasswordPage.class)
    }
    
    def RequestCycle newRequestCycle(Request request, Response response) {
        new JpaRequestCycle(this, (WebRequest) request, response)
    }
    
    def BookingSession newSession(Request request, Response response) {
        new BookingSession(request)
    }
    
    def IRequestCycleProcessor newRequestCycleProcessor() {
        new UrlCompressingWebRequestProcessor()
    }
    
    def ISessionStore newSessionStore() {
        if(configurationType.equalsIgnoreCase(DEVELOPMENT)) {
            println "development mode, using http session store"
            new HttpSessionStore(this)
        } else {
            super.newSessionStore()
        }
    }
    
    def IConverterLocator newConverterLocator() {
        ConverterLocator converterLocator = new ConverterLocator()
        BigDecimalConverter converter = new BigDecimalConverter() {
            @Override
            public NumberFormat getNumberFormat(Locale locale) {
                return DecimalFormat.getCurrencyInstance(Locale.US)
            }
        }
        converterLocator.set(BigDecimal.class, converter)
        return converterLocator
    }
	
}

