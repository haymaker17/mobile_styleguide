//
//  Authenticate.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "Authenticate.h"
#import "MsgControl.h"
#import "FormatUtils.h"
#import "DataConstants.h"
#import "CCDateUtilities.h"

#import "ExSystem.h"

@interface Authenticate()
@property (nonatomic, readwrite, assign) BOOL isInSalesForceNode;
@property (nonatomic, readwrite, copy) NSString *salesForceToken;
@property (nonatomic, readwrite, copy) NSString *salesForceUrl;
@end

@interface NSURLRequest (DummyInterface)
+ (BOOL)allowsAnyHTTPSCertificateForHost:(NSString*)host;

@end

@implementation Authenticate

@synthesize path;

@synthesize userName;
@synthesize pin, password, pinOrPassword;
@synthesize authenticated;
@synthesize entityType, productOffering;
@synthesize sessionID, accessToken, accessTokenSecret;
@synthesize timedOut;
@synthesize roles;
@synthesize currentElement;
@synthesize remoteWipe;

@synthesize buildString;
@synthesize crnCode;
@synthesize userMessages;
@synthesize userMsg;
@synthesize disableAutoLogin;
@synthesize inSiteSetting;
@synthesize curSiteSetting, expenseCtryCode, isTripItLinked, isTripItEmailAddressConfirmed, dateCreated, dateResponseReceived, dateResponseParsed;
@synthesize needAgreement;
@synthesize commonResponseCode;
@synthesize commonResponseSystemMessage;
@synthesize commonResponseUserMessage;

static NSMutableDictionary* userContactMap = nil;

+ (void)initialize
{
	if (self == [Authenticate class]) 
	{
        // Perform initialization here.
		userContactMap = [[NSMutableDictionary alloc] init];
		userContactMap[@"FirstName"] = @"FirstName";
		userContactMap[@"LastName"] = @"LastName";
		userContactMap[@"Mi"] = @"Mi";
		userContactMap[@"Email"] = @"Email";
	}
}

#pragma mark -
#pragma mark XML Init Methods
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	[super parseXMLFileAtData:webData];
	self.currentElement = nil;
    self.isAccountExpired = NO;
}

-(void) flushData
{
	self.pin = @"";
	self.userName = @"";
	self.sessionID = nil;
	self.timedOut = nil;
	self.remoteWipe = @"N";
    self.needAgreement = @"false";			//set this to false after testing

    // salesforce login info
    self.salesForceToken = nil;
    self.salesForceUrl = nil;
    self.isInSalesForceNode = NO;
    self.isAccountExpired = NO;
}

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	self.dateResponseReceived = [NSDate date];
	[self flushData];
	[self parseXMLFileAtData:data];
    self.dateResponseParsed = [NSDate date];
}


#pragma mark -
#pragma mark Message Init
-(id)init
{
	self = [super init];
    if (self)
    {
        self.dateCreated = [NSDate date];
        [self flushData];
    }
	return self;
}

-(BOOL) isTestDrive:(NSMutableDictionary *) pBag
{
    return pBag[@"TEST_DRIVE"] != nil;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message

	self.userName = parameterBag[@"USER_ID"];
	self.pin = parameterBag[@"PIN"];
	self.password = parameterBag[@"PASSWORD"];
    self.pinOrPassword = parameterBag[@"PIN_OR_PASSWORD"];
	
    NSString *isCorpSSO = (NSString *)parameterBag[@"IS_CORP_SSO"];
    
	NSString* loginWithConcurAccessTokenStr = parameterBag[@"LOGIN_WITH_CONCUR_ACCESS_TOKEN"];
	bool loginWithConcurAccessToken = (loginWithConcurAccessTokenStr != nil || [loginWithConcurAccessTokenStr isEqualToString:@"YES"]);
		
    if ([isCorpSSO length])
        self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/CorpSsoLogin",[ExSystem sharedInstance].entitySettings.uri];
    else if([self isTestDrive:parameterBag])
        self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/RegisterTestDriveUser", [ExSystem sharedInstance].entitySettings.uri];
	else if (loginWithConcurAccessToken)
        self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/AutoLoginV3", [ExSystem sharedInstance].entitySettings.uri];
	else if ([pinOrPassword lengthIgnoreWhitespace])
        self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/PPLogin", [ExSystem sharedInstance].entitySettings.uri];
    else if ([password length])
        self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/PasswordLoginV2", [ExSystem sharedInstance].entitySettings.uri];
	else
        self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/Login", [ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:AUTHENTICATION_DATA State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
    
    if (loginWithConcurAccessToken)
        msg.oauth2AccessToken = [ExSystem sharedInstance].concurAccessToken;
	
    if (![ExSystem sharedInstance].isCorpSSOUser)
    {
        if ([password length] || [pinOrPassword length] || [self isTestDrive:parameterBag])
            msg.numOauthLegs = 2;	// Sign with two-legged oauth (signature specifies the consumer application, but not a specific user)
    }

    // Compose msg body
    if ([isCorpSSO length])
    {
        [msg setBody:[self makeXMLBodyCorpSSO:parameterBag[@"SESSION_ID"]]];    
    }
    else if (!loginWithConcurAccessToken) // If we're not auto-logging, then an xml body is required.
	{
        if ([self isTestDrive:parameterBag])
        {
            [msg setBody:[self makeXMLBody:parameterBag]];
        }
        else
        {
			[msg setBody:[self makeXMLBody]];
        }
    }
    else
    {
        [msg setBody:[self makeXMLBodyLocaleOnly]];
    }
    

	return msg;
}

-(NSString *)makeXMLBodyLocaleOnly
{
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<Credentials>"];
    //MOB-16368 - Get preferred language
	NSString* preferredLang = [Localizer getPreferredLanguage];
	if (preferredLang != nil)
	{
		[bodyXML appendString:@"<Locale>"];
		[bodyXML appendString:preferredLang];
		[bodyXML appendString:@"</Locale>"];
	}
	[bodyXML appendString:@"</Credentials>"];
	return bodyXML;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)pBag
{
    /*
     ---> Test Drive request
     <Credentials>
     <CtryCode>US</CtryCode>
     <Locale>en</Locale>
     <LoginID>tu1@bnxtest9.com</LoginID>                         <=User FB email
     <Password>0000aaAA</Password>                               <=Must be alpha numeric
     </Credentials>
     
     */
    //knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<Credentials>"];
    
    if ([self isTestDrive:pBag]) {
        NSString *countryCode = [[NSLocale currentLocale] objectForKey: NSLocaleCountryCode];
        [bodyXML appendString:@"<CtryCode>"];
		[bodyXML appendString:countryCode];
		[bodyXML appendString:@"</CtryCode>"];
    }
    //MOB-16368 - Get preferred language
	NSString* preferredLang = [Localizer getPreferredLanguage];
	if (preferredLang != nil)
	{
		[bodyXML appendString:@"<Locale>"];
		[bodyXML appendString:preferredLang];
		[bodyXML appendString:@"</Locale>"];
	}

    
	[bodyXML appendString:@"<LoginID>%@</LoginID>"];
	
	if ([pinOrPassword length])
		[bodyXML appendString:@"<Password>%@</Password>"];
	else if ([password length])
		[bodyXML appendString:@"<Password>%@</Password>"];
	else
		[bodyXML appendString:@"<Pin>%@</Pin>"];
    
    NSString *secret = [pinOrPassword length] ? pinOrPassword : ([password length] ? password : pin);
	
	//[bodyXML appendString:@"<UseTestMessages>true</UseTestMessages>"];
	[bodyXML appendString:@"</Credentials>"];
	// MOB-16209 - Encode password also
	NSString *returnVal = [NSString stringWithFormat:bodyXML, [FormatUtils makeXMLSafe:userName], [FormatUtils makeXMLSafe:secret]];
    //NSLog(@"returnVal = %@", returnVal);
	return returnVal;

}

-(NSString *)makeXMLBody
{
    return [self makeXMLBody:nil];
}

-(NSString *)makeXMLBodyCorpSSO:(NSString *)sessionKey
{
    if (sessionKey != nil) 
    {
        self.sessionID = sessionKey;
    }
    
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<WebSession>"];
    
    // MOB-14355 Add locale to request body.
    //
    NSUserDefaults* defs = [NSUserDefaults standardUserDefaults];
	NSArray* languages = [defs objectForKey:@"AppleLanguages"];
	NSString* preferredLang = languages[0];
	if (preferredLang != nil)
	{
		[bodyXML appendString:@"<Locale>"];
		[bodyXML appendString:preferredLang];
		[bodyXML appendString:@"</Locale>"];
	}
    
	[bodyXML appendString:@"<SessionId>%@</SessionId>"];
    [bodyXML appendString:@"</WebSession>"];
	
	NSString *returnVal = [NSString stringWithFormat:bodyXML, [FormatUtils makeXMLSafe:sessionKey]];
    return returnVal;
}

#pragma mark -
#pragma mark URL Methods
- (BOOL)connection:(NSURLConnection *)connection
canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *) space 
{
	if([[space authenticationMethod] 
		isEqualToString:NSURLAuthenticationMethodServerTrust]) {
		// Note: this is presently only called once per server (or URL?) until
		//       you restart the app
		//if(shouldAllowSelfSignedCert) {
		return YES; // Self-signed cert will be accepted
		//} else {
		//	return NO;  // Self-signed cert will be rejected
		//}
		// Note: it doesn't seem to matter what you return for a proper SSL cert
		//       only self-signed certs
	}
	// If no other authentication is required, return NO for everything else
	// Otherwise maybe YES for NSURLAuthenticationMethodDefault and etc.
	return NO;
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{

}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
//	NSString * errorString = [NSString stringWithFormat:@"Unable to authenticate from web site (Error code %i )", [parseError code]];
	//NSLog(@"error parsing XML: %@", errorString);
	
//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
//	[errorAlert show];
//	[errorAlert release];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;

	buildString = [[NSMutableString alloc] init];

	if ([elementName isEqualToString:@"SalesForceToken"])
	{
        self.isInSalesForceNode = YES;
	}
	else if ([elementName isEqualToString:@"Session"])
    {
		self.sessionID = @"";
		self.authenticated = @"YES";
	}
	else if ([elementName isEqualToString:@"Messages"])
	{
		if (userMessages != nil) 
		{
			[userMessages removeAllObjects];
		}
		else 
		{
			self.userMessages = [[NSMutableArray alloc] init];
		}
	}
	else if ([currentElement isEqualToString:@"Message"])
	{
		self.userMsg = [[UserMessage alloc] init];
	}
    else if ([currentElement isEqualToString:@"LoginResult"] || [currentElement isEqualToString:@"Response"])
    {
        [[ExSystem sharedInstance] loadSiteSettings];
        [[ExSystem sharedInstance] clearSiteSettings];
    }
    else if ([currentElement isEqualToString:@"SiteSetting"])
    {
		self.inSiteSetting = YES;
		self.curSiteSetting = [[ExSiteSetting alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"SalesForceToken"])
	{
        self.isInSalesForceNode = NO;
	}
    else if ([elementName isEqualToString:@"Message"])
	{
		[userMessages addObject:userMsg];
	}
	else if ([elementName isEqualToString:@"SiteSetting"])
	{
		self.inSiteSetting = FALSE;
		if (curSiteSetting != nil)
		{
            [[ExSystem sharedInstance] saveSiteSetting:curSiteSetting.value type:curSiteSetting.type name:curSiteSetting.name];
			self.curSiteSetting = nil;
		}
	}
    else if ([elementName isEqualToString:@"HasRequiredCustomFields"] ||
             [elementName isEqualToString:@"ProfileStatus"])
    {
        // User settings are name/value pairs as child nodes of LoginResult
        [[ExSystem sharedInstance] saveUserSetting:buildString name:elementName];
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	NSString* propName = userContactMap[currentElement];
	if (propName != nil)
	{
        // are you kidding me.  we build the method name from a string in a dictionary...
		[[ExSystem sharedInstance].entitySettings setValue:string forKey:propName];
	}
    else if (self.isInSalesForceNode)
    {
    	// currently the node is prefixed with a: I check for the other style too just in case this changes.
        if ([currentElement isEqualToString:@"a:AccessToken"] || [currentElement isEqualToString:@"AccessToken"])
        {
            self.salesForceToken = buildString;
        }
        else if ([currentElement isEqualToString:@"a:InstanceUrl"] || [currentElement isEqualToString:@"InstanceUrl"])
        {
            self.salesForceUrl = buildString;
        }
        return;
    }
	else if (inSiteSetting)
	{
		[buildString appendString:string];
		if ([currentElement isEqualToString:@"Name"])
		{
			self.curSiteSetting.name = buildString;
		}
		else if ([currentElement isEqualToString:@"Type"])
		{
			self.curSiteSetting.type = buildString;
		}
		else if ([currentElement isEqualToString:@"Value"])
		{
			self.curSiteSetting.value = buildString;
		}
	}
    else if ([currentElement isEqualToString:@"HasRequiredCustomFields"] ||
             [currentElement isEqualToString:@"ProfileStatus"])
    {
		[buildString appendString:string];
    }
    else if ([currentElement isEqualToString:@"HasTravelRequest"] ||
             [currentElement isEqualToString:@"IsRequestUser"] ||
             [currentElement isEqualToString:@"IsRequestApprover"]){
        
        [[ExSystem sharedInstance] saveUserSetting:string name:currentElement];
    }
	else if ([currentElement isEqualToString:@"CompanyName"])
	{
		[buildString appendString:string];
		[[ExSystem sharedInstance].entitySettings setCompanyName:buildString];
	}
	else if ([currentElement isEqualToString:@"ID"])
	{
		self.sessionID = string;
		self.authenticated = @"YES";
	}
	else if ([currentElement isEqualToString:@"TimeOut"])
	{
		self.timedOut = string;
	}
	else if ([currentElement isEqualToString:@"ExpenseCtryCode"])
	{
		self.expenseCtryCode = string;
        [ExSystem sharedInstance].sys.expenseCtryCode = string;
	}
	else if ([currentElement isEqualToString:@"RolesMobile"])
	{
		self.roles = string;
	}
	else if ([currentElement isEqualToString:@"EntityType"])
	{
		self.entityType = string;
	}
    else if ([currentElement isEqualToString:@"ProductOffering"])
    {
        self.productOffering = string;
    }
    else if ([currentElement isEqualToString:@"NeedSafeHarborAgreement"])
    {
        self.needAgreement = buildString;
    }
	else if ([currentElement isEqualToString:@"RemoteWipe"])
	{
		self.remoteWipe = string;
	}
	else if ([currentElement isEqualToString:@"TwitterUrl"])
	{
        [ExSystem sharedInstance].entitySettings.twitterUrl = string;
	}
	else if ([currentElement isEqualToString:@"UserCrnCode"])
	{
		self.crnCode = string;
	}
    else if ([currentElement isEqualToString:@"UserId"])
	{
		self.userId = string;
	}
    else if ([currentElement isEqualToString:@"IsEmailAddressConfirmed"])
    {
        if([string isEqualToString:@"true"])
            self.isTripItEmailAddressConfirmed = YES;
        else
            self.isTripItEmailAddressConfirmed = NO;
        
        [ExSystem sharedInstance].isTripItEmailAddressConfirmed = self.isTripItEmailAddressConfirmed;
    }
    else if ([currentElement isEqualToString:@"IsLinked"])
    {
        if([string isEqualToString:@"true"])
            self.isTripItLinked = YES;
        else
            self.isTripItLinked = NO;   
        
        [ExSystem sharedInstance].isTripItLinked = self.isTripItLinked;
    }
	else if ([currentElement isEqualToString:@"Body"])
	{
		userMsg.msgBody = (userMsg.msgBody != nil)?[userMsg.msgBody stringByAppendingString:string]:string;
	}
	else if ([currentElement isEqualToString:@"Title"])
	{
		userMsg.msgTitle = string;
	}	
	else if ([currentElement isEqualToString:@"Url"])
	{
		userMsg.msgURL = string;
		//TODO: check for different url schemes
		if (userMsg.msgURL != nil && ![userMsg.msgURL hasPrefix:@"http://"] ) 
		{
			userMsg.msgURL = nil;
		}
	}
	else if ([currentElement isEqualToString:@"DisableAutoLogin"])
	{
		self.disableAutoLogin = string;
	}
	else if ([currentElement isEqualToString:@"AccessTokenKey"])
	{
        [buildString appendString:string];
		self.accessToken = buildString;
	}
	else if ([currentElement isEqualToString:@"AccessTokenSecret"])
	{
        [buildString appendString:string];
		self.accessTokenSecret = buildString;
	}
    else if ([currentElement isEqualToString:@"ServerUrl"]) {
        [buildString appendString:string];
        if (![[ExSystem sharedInstance].entitySettings.uri isEqualToString:buildString]) {
            NSLog(@"Switching to URL: %@", buildString);
            [ExSystem sharedInstance].entitySettings.uri = buildString;
        }
    }
    else if ([currentElement isEqualToString:@"Code"])
    {
        // New element present in MWSResponse messages
        self.commonResponseCode = string;
    }
    else if ([currentElement isEqualToString:@"SystemMessage"])
    {
        // New element present in MWSResponse messages
        [buildString appendString:string];
        self.commonResponseSystemMessage = buildString;
    }
    else if ([currentElement isEqualToString:@"UserMessage"])
    {
        // New element present in MWSResponse messages
        [buildString appendString:string];
        self.commonResponseUserMessage = buildString;
    }
    else if ([currentElement isEqualToString:@"AccountExpirationDate"])
    {
        // Date has milli seconds 2014-06-02T05:14:55.497
        NSLocale *enUSPOSIXLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"];
        NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:@"YYYY-MM-DD'T'HH:mm:ss.SSS" timeZoneWithAbbreviation:@"GMT" locale:enUSPOSIXLocale];
        self.accountExpirationDate = [dateFormatter dateFromString:string];

        NSDateComponents *otherDay = [[NSCalendar currentCalendar] components:NSEraCalendarUnit|NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit fromDate:self.accountExpirationDate];
        NSDateComponents *today = [[NSCalendar currentCalendar] components:NSEraCalendarUnit|NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit fromDate:[NSDate date]];
        
        if([today day] == [otherDay day] &&
           [today month] == [otherDay month] &&
           [today year] == [otherDay year] &&
           [today era] == [otherDay era]) {
            self.isAccountExpired = YES;
            // Send a notification event to say that this account is expired.
            [[NSNotificationCenter defaultCenter] postNotificationName:NotificationOnAccountExpired object:self];
        }
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser
{
	[[ExSystem sharedInstance] saveSystem];
    [[ExSystem sharedInstance] saveSettings];
    [[ExSystem sharedInstance] loadSiteSettings];

    [[ExSystem sharedInstance] saveSalesForceToken:self.salesForceToken andUrl:self.salesForceUrl];
}

@end
