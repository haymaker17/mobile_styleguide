//
//  RequestController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "Config.h"
#import "RequestController.h"
#import "Msg.h"
#import "MCLogging.h"
#import "PostQueue.h"
#import "ExSystem.h" 

#import "ErrorData.h"
#import "MobileAlertView.h"
#import "ConcurMobileAppDelegate.h"
#import "ApplicationLock.h"
#import "ConcurConsumer.h"
#import "Msg.h"



//hack to accept self signed certs
@interface NSURLRequest (DummyInterface)
+ (BOOL)allowsAnyHTTPSCertificateForHost:(NSString*)host;
@end


@implementation RequestController

@synthesize ipAddress;
@synthesize msg;
@synthesize msgControl;
//@synthesize uuid;
@synthesize inprocessing;
@synthesize timeOfRequest;

#pragma mark Initializing Methods
-(void) init:(Msg *)msgToUse MessageControl:(MsgControl *)msgControlToUse
{
	self.msg = msgToUse;
	self.msgControl = msgControlToUse;

    if ([Config isNetworkDebugEnabled]) {
        NSLog(@"URL: %@", self.msg.uri);
        NSLog(@"Request: %@", self.msg.body);
    }

	[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::init(%@)", msg.idKey] Level:MC_LOG_DEBU];
	
    [self send];
}

/*-(void) initPostMsg:(Msg *)msgToUse MessageControl:(MsgControl *)msgControlToUse
{
    inprocessing = FALSE;
    [self init:msgToUse MessageControl:msgControlToUse];
}*/

//Special method to invoke the requester directly
-(void) initDirect:(Msg *)msgToUse MVC:(NSObject<ExMsgRespondDelegate> *)mobileVC
{
	self.msg = msgToUse;
	self.respondToMvc = mobileVC;
    if ([Config isNetworkDebugEnabled]) {
        NSLog(@"URL: %@", self.msg.uri);
        NSLog(@"Request: %@", self.msg.body);
    }

    [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::initDirect(%@)", msg.idKey] Level:MC_LOG_DEBU];
    
    [self send];
}

-(void) send
{
    if (!inprocessing)
    {
        if (![ExSystem connectedToNetwork])
        {
            [self routeMsgToResponderReceivedResponse:NO];
            return;
        }

        inprocessing = TRUE;
        if (msg.request != nil) 
        {
            [self makeRequest:msg.request];
        }
        else
        {
            NSString *useThisURL = [[NSString alloc] initWithString: msg.uri];
            [self makeRequest:useThisURL requestType:msg.idKey soapAction:@""];	
        }
    }
}

//formats a soap header for the makeRequest call
-(void) createSOAPHeader:(NSString *)something
{
//    NSString *soapMsg = [NSString stringWithFormat:@"<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"> <soap:Body><FindCountryAsXml xmlns=\"http://www.ecubicle.net/webservices/\"><V4IPAddress>%@</V4IPAddress></FindCountryAsXml></soap:Body></soap:Envelope>"
//						,ipAddress];
//	something = soapMsg;
	
}

#pragma mark HTTPS override method
//the following method allows us past some of the limitations associated with https and NSURL
- (BOOL)connection:(NSURLConnection *)connection
canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *) space 
{
	if([[space authenticationMethod] 
		isEqualToString:NSURLAuthenticationMethodServerTrust]) {
		// Note: this is presently only called once per server (or URL?) until
		//       you restart the app
		//if(shouldAllowSelfSignedCert) {
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::connection(%@) Checking SSL Authentication", msg.idKey] Level:MC_LOG_DEBU];
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


-(BOOL) isMsgForCurrentUser
{
	// If the message is not bound to a particular session, then let it through
	// to the current user.
	if (self.msg.header == nil)
		return TRUE;
	
	// We got this far, so the message is bound to a particular session.  However,
	// if we don't have session information, then refuse the message.
	if ([ExSystem sharedInstance].sessionID == nil)
		return FALSE;
		
	if ([ExSystem sharedInstance].sessionID != nil &&							// IF we have a session id AND
		[[ExSystem sharedInstance].sessionID length] != 0 &&						// it's not the empty string AND
		![[ExSystem sharedInstance].sessionID isEqualToString:@"OFFLINE"] &&		// it's not "OFFLINE" AND
		[[ExSystem sharedInstance].sessionID isEqualToString:self.msg.header])	// it matches the header
		return TRUE;											// THEN accept the message.
		
	return FALSE; // Refusal
}


#pragma mark Fetch the Data Methods
-(void) routeMsgToResponderReceivedResponse:(BOOL)receivedResponse
{
    if(self.msgControl != nil && receivedResponse)
        [msgControl msgDone:msg CameFromCache:@"NO"];	//tell message control that we are done, and send the msg back for handling
	else if(self.msg.responder.respondToMvc != nil)
	{
		if ([self.msg.responder.respondToMvc respondsToSelector:@selector(didProcessMessage:)])
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::routeMsgToResponder invoking %@::didProcessMessage:%@", [self.msg.responder.respondToMvc class], msg.idKey] Level:MC_LOG_DEBU];
			[self.msg.responder.respondToMvc didProcessMessage:msg];
		}
	}
	else if(self.respondToMvc != nil)
	{
		if ([self.respondToMvc respondsToSelector:@selector(didProcessMessage:)])
		{
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::routeMsgToResponder invoking %@::didProcessMessage:%@", [respondToMvc class], msg.idKey] Level:MC_LOG_DEBU];
			[respondToMvc didProcessMessage:msg];
		}
	}
    else
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::routeMsgToResponder. No delegate for message %@", msg.idKey] Level:MC_LOG_DEBU];
}

-(void) prepareRequest:(NSMutableURLRequest *)req requestType:(NSString *)reqType 
{
    // Always use the current session ID, and record the actual session ID in msg for response delivery
    
	if ([ExSystem sharedInstance].sessionID != nil && [[ExSystem sharedInstance].sessionID length] > 0 && ![msg.idKey isEqualToString:REGISTER_DATA])
		msg.header = [ExSystem sharedInstance].sessionID;
    
    if(![reqType isEqualToString:IMAGE])
        [req addValue:msg.header forHTTPHeaderField:@"X-SessionID"];
    
    if (msg.uuid != nil)
        [req addValue:msg.uuid forHTTPHeaderField:@"X-MsgID"];

	// Used in OAuth 1.0 protocol
	if (msg.numOauthLegs > 0)
	{
		ConcurConsumer* consumer = [[ConcurConsumer alloc] init];
		NSString* oauthHeader = [consumer oauthHeaderLegs:msg.numOauthLegs httpMethod:msg.method url:msg.uri];
		if (oauthHeader != nil)
			[req addValue:oauthHeader forHTTPHeaderField:@"Authorization"];
	}
    
    // Used in OAuth 2.0 protocol
    if (self.msg.oauth2AccessToken != nil)
    {
        [req addValue:[NSString stringWithFormat:@"OAuth %@", self.msg.oauth2AccessToken] forHTTPHeaderField:@"Authorization"];


        // Salesforce specific http header, this should be safe since it's an X- setting.  Other sites should all ignore this.  -EC
        [req addValue:@"false" forHTTPHeaderField:@"X-Chatter-Entity-Encoding"]; // Prevents HTML encoding inside JSON responses
    }
    
	if (self.msg.contentType != nil)
		[req addValue:self.msg.contentType forHTTPHeaderField:@"Content-Type"];
    
    // MOB-5725
	[req addValue:@"gzip" forHTTPHeaderField:@"Accept-Encoding"];
    
    if (self.msg.expectedContentLength > 0)
    {
        NSString* expectedContentLengthAsString = [NSString stringWithFormat:@"%i", self.msg.expectedContentLength];
		[req addValue:expectedContentLengthAsString forHTTPHeaderField:@"X-ExpectedContentLength"];
    }
    
    if ([self.msg.idKey isEqualToString:CORP_SSO_AUTHENTICATION_DATA]) 
    {
        NSString *host = [ExSystem sharedInstance].entitySettings.uri;
        host = [host stringByReplacingOccurrencesOfString:@"https://" withString:@""];
        
        if (host != nil) 
        {
            [req addValue:host forHTTPHeaderField:@"Host"];
        }
        
        [req setHTTPShouldHandleCookies:YES];
    }
    
	if ([self.msg.idKey isEqualToString:AUTHENTICATION_DATA] ||
		[self.msg.idKey isEqualToString:PWD_LOGIN_DATA])
		[req addValue:[MCLogging getDeviceDesc] forHTTPHeaderField:@"User-Agent"];
    
	if (self.msg.body != nil)
		[req setHTTPBody: [self.msg.body dataUsingEncoding:NSUTF8StringEncoding]];
	else if (self.msg.bodyData != nil)
		[req setHTTPBody:self.msg.bodyData];
	
	[req setHTTPMethod:self.msg.method];
}

-(void) makeSynchroRequest:(NSMutableURLRequest*) req
{
    //this means that I am going to be launching this request inside of a thread that I am maintaining.  There is a collision of threads when I call 
    //NSURLConnection asynch, through a self made thread that is managing it's own pool.
    NSURLResponse *response = nil;
    NSError *error = nil;
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::makeRequest(%@) Doing a Synchro fetch", msg.idKey] Level:MC_LOG_DEBU];
    NSData *data = [NSURLConnection sendSynchronousRequest: req returningResponse: &response error: &error]; 
    int statusCode = (int)[((NSHTTPURLResponse *)response) statusCode];
    //NSLog(@"Synchro Response StatusCode=%d", statusCode);
    msg.responseCode = statusCode;
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::makeRequest(%@) Synchro Response code=%d", msg.idKey, statusCode] Level:MC_LOG_DEBU];
    
    NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
    
    if(statusCode >= 400)
    {
        if (error != nil)
        {
//            [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::makeRequest(%@) Synchro %d %@ %@", msg.idKey, statusCode, theXML, [error localizedDescription]] Level:MC_LOG_ERRO];
            msg.errBody = [error localizedDescription];	
        }
        msg.errCode = [NSString stringWithFormat:@"%d", statusCode];
    } 
    // MOB-1633 - need to handle bad connection error upon session autologin
    // Upon connection failure statusCode is empty and NSError is filled with info
    else if (statusCode == 0 && error != nil)
    {
//        [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::makeRequest(%@) Synchro %d %@ %@", msg.idKey, [error code], theXML, [error localizedDescription]] Level:MC_LOG_ERRO];
        msg.errCode = [NSString stringWithFormat:@"%ld", (long)[error code]];
        msg.errBody = [error localizedDescription];	
    }
    
    if ([self.msg.method isEqualToString:@"POST"])
    {
        // Remove synchro post msg from queue regardless of response status
        // Since we want notify the user and let the UI handle the situation
        [[PostQueue getInstance] finishPostMsg:self result:@"NORMAL"];
    }
    inprocessing = FALSE;
    //NSLog(@"Synchro Error = %@", [error localizedDescription]);
    
    //NSLog(@"Synchro theXML: %@", theXML);
    self.msg.xmlString = theXML;
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::makeRequest(%@) Synchro XML = %@", msg.idKey, theXML] Level:MC_LOG_DEBU];
    
    msg.data = data;
    
    UIApplication* app = [UIApplication sharedApplication]; 
    app.networkActivityIndicatorVisible = NO;
    
    BOOL receivedResponse = (error != nil);
    [self routeMsgToResponderReceivedResponse:receivedResponse];
}

-(void) makeRequest:(NSMutableURLRequest *)request
{
   	self.timeOfRequest = [NSDate date];
	
	//on title bar show network activity indicator
	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = YES; // to stop it, set this to NO 
	
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    [request setTimeoutInterval:msg.timeoutInterval];
	
	//To establish the connection with the web service, you use the NSURLConnection class together with the request object just created:
	
	if ((msg.parameterBag)[@"SYNCHRO"] != nil) 
	{
        [self makeSynchroRequest: request];
        
		return;
	}
	else 
	{   
		conn = [[NSURLConnection alloc] initWithRequest:request delegate:self];
		if (conn) 
		{
			webData = [NSMutableData data];
		} 
	}     
}

-(void) makeRequest:(NSString *)strURL requestType:(NSString *)reqType soapAction:(NSString *) soapAction
{
	self.timeOfRequest = [NSDate date];
	
	//on title bar show network activity indicator
	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = YES; // to stop it, set this to NO 
	
	//Next, you create a URL load request object using instances of the NSMutableURLRequest and NSURL objects
    NSString *trimmedURL = [strURL stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	NSURL *url = [[NSURL alloc] initWithString: trimmedURL];
    NSMutableURLRequest *req = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:msg.timeoutInterval];
	
    [self prepareRequest:req requestType:reqType];
	
	//To establish the connection with the web service, you use the NSURLConnection class together with the request object just created:
	
	if ((msg.parameterBag)[@"SYNCHRO"] != nil) 
	{
        [self makeSynchroRequest: req];

		return;
	}
	else 
	{
		conn = [[NSURLConnection alloc] initWithRequest:req delegate:self];
		if (conn) 
		{
			webData = [NSMutableData data];
		} 
	}
}


#pragma mark Connection Delegate Methods
//When data starts streaming in from the web service, the connection:didReceiveResponse: method will be called, which you need to implement here
-(void) connection:(NSURLConnection *) connection 
didReceiveResponse:(NSURLResponse *) response 
{
    [webData setLength: 0];
    NSHTTPURLResponse* httpResp = (NSHTTPURLResponse *)response;
//    NSDictionary *headers = [httpResp allHeaderFields];
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Headers Content-Encoding %@", [headers objectForKey:@"Content-Encoding"]] Level:MC_LOG_DEBU];
    
	int statusCode = (int)[((NSHTTPURLResponse *)response) statusCode];
	msg.responseCode = statusCode;// [NSString stringWithFormat:@"%d", statusCode];
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::didReceiveResponse(%@) Response Code = %d", msg.idKey, statusCode] Level:MC_LOG_DEBU];

    if (statusCode == 200 && ([msg.idKey isEqualToString:@"OOPE_IMAGE"] || [msg.idKey isEqualToString:@"RECEIPT_IMAGE"]))
    {
        NSDictionary *respDict = (NSDictionary*)[((NSHTTPURLResponse *)response) allHeaderFields];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::didReceiveResponse(%@) Image Content-type = %@", msg.idKey, respDict[@"Content-Type"]] Level:MC_LOG_DEBU];
        [msg setContentType:respDict[@"Content-Type"]];
    }
    else if ([msg.idKey isEqualToString:CORP_SSO_AUTHENTICATION_DATA])
    {
        (msg.parameterBag)[@"HTTP_RESPONSE"] = httpResp;
    }

	if(statusCode >= 400)
	{
		msg.errCode = [NSString stringWithFormat:@"%d", statusCode];
		//NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
		ErrorData *ed = [[ErrorData alloc] init];
		[ed parseXMLFileAtData:webData];
	}
}


//Note that the preceding code initializes the length of webData to 0. As the data progressively comes in from the web service, the connection:didReceiveData: method will be called repeatedly. You use the method to append the data received to the webData object:
-(void) connection:(NSURLConnection *) connection 
	didReceiveData:(NSData *) data 
{
	[webData appendData:data];
}


//If there is an error during the transmission, the connection:didFailWithError: method will be called
-(void) connection:(NSURLConnection *) connection didFailWithError:(NSError *) error 
{
    // Remove post msg from queue
    if ([self.msg.method isEqualToString:@"POST"])
    {
        [[PostQueue getInstance] finishPostMsg:self result:@"NETWORK_FAILURE"];
        inprocessing = FALSE;
    }
    
	
	if (![self isMsgForCurrentUser])
		return;

	UIApplication* app = [UIApplication sharedApplication];
	app.networkActivityIndicatorVisible = NO;
	
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::didFailWithError(%@) LocalizedDescription = %@, LocalizedFailureReason = %@, LocalizedRecoverySuggestion = %@", msg.idKey, [error localizedDescription], [error localizedFailureReason], [error localizedRecoverySuggestion]] Level:MC_LOG_ERRO];

	if(![msg.idKey isEqualToString:VENDOR_IMAGE] && ![msg.idKey isEqualToString:IMAGE])
	{
		// If the app is backgrounded after a request was sent but before a response was received,
		// then we will get a time out error when the app returns to the foreground.  We want to do not want
		// to alert the user of errors arising from requests that were made before the app was backgrounded.
		//
		// If the app has not been inactive OR
		// we do not know the time this request was sent OR
		// the app became inactive BEFORE the request was sent (meaning it was sent after the app became inactive and then active again)
		// Then alert the user of the error
		//
		ApplicationLock *appLock = [ApplicationLock sharedInstance];
		NSString* inactivityBeginDateAsString = (appLock.inactivityBeginDate == nil ? @"nil" : [appLock.inactivityBeginDate description]);
		NSString* timeOfRequestAsString = (self.timeOfRequest == nil ? @"nil" : [self.timeOfRequest description]);
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::connection:didFailWithError inactivityBeginDate is %@, timeOfRequest is %@", inactivityBeginDateAsString, timeOfRequestAsString] Level:MC_LOG_DEBU];
		if (appLock.inactivityBeginDate == nil ||
			self.timeOfRequest == nil ||
			[appLock.inactivityBeginDate compare:self.timeOfRequest] == NSOrderedAscending)
		{
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:[Localizer getLocalizedText:@"Connection Error"]
								  message:[error localizedDescription]
								  // MOB-1633 - RequestController gets released before alert box is shown, so no delegate for these alert boxes.
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
	}
	
	msg.errBody = [error localizedDescription];
    [self routeMsgToResponderReceivedResponse:NO];
}

// MOB-9400 handle site down error
-(NSString*) translateServerError:(NSString*) errMsg
{
    if (errMsg != nil && [@"Expense_Site_Down" isEqualToString:errMsg])
    {
        return [@"ERROR_EXPENSE_SITE_DOWN" localize];
    }
    else
        return errMsg;
}

//When the connection has finished and succeeded in downloading the response, the connectionDidFinishLoading: method will be called:
-(void) connectionDidFinishLoading:(NSURLConnection *) connection 
{
	inprocessing = FALSE;

	if (![self isMsgForCurrentUser] && ![msg.idKey isEqualToString:REGISTER_DATA] && ![msg.idKey isEqualToString:@"GET_TRIPIT_CACHE_DATA"]) //MOB-4619, pin registration was being short circuited
	{
		// Remove post msg from queue ssss
		if ([self.msg.method isEqualToString:@"POST"])
		{
			[[PostQueue getInstance] finishPostMsg:self result:@"NORMAL"];
		}

        
        UIApplication* app = [UIApplication sharedApplication]; 
        app.networkActivityIndicatorVisible = NO;
        
		return;
	}
//	[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::makeRequest(%@) END", msg.idKey == nil? @"":msg.idKey] Level:MC_LOG_DEBU];
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::connectionDidFinishLoading(%@)", msg.idKey] Level:MC_LOG_DEBU];
    if ([Config isNetworkDebugEnabled]) {
        NSString *theXML = [[NSString alloc] initWithBytes: [webData mutableBytes] length:[webData length]  encoding:NSUTF8StringEncoding];
        NSLog(@"Response: %@", theXML);
    }
    
	if ([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA]) 
    {
        NSTimeInterval secondsBetween = [self.timeOfRequest timeIntervalSinceDate:msg.dateOfData];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"Receipt upload rtt %f sec", secondsBetween] Level:MC_LOG_DEBU];
    }
    
	if (![msg.idKey isEqualToString:AUTHENTICATION_DATA] &&
		![msg.idKey isEqualToString:PWD_LOGIN_DATA] &&
		![msg.idKey isEqualToString:VENDOR_IMAGE] &&
		![msg.idKey isEqualToString:IMAGE] &&
        ![msg.idKey isEqualToString:UPLOAD_IMAGE_DATA] &&
        ![msg.idKey isEqualToString:RECEIPT_STORE_RECEIPTS] &&
        ![msg.idKey isEqualToString:CAR_IMAGE]  &&
        ![msg.idKey isEqualToString:@"RECEIPT_IMAGE"]  &&
        ![msg.idKey isEqualToString:HOTEL_IMAGES] &&
        ![msg.idKey isEqualToString:CORP_SSO_AUTHENTICATION_DATA] &&
        ![msg.idKey isEqualToString:VALIDATE_SESSION] &&
        ![msg.idKey isEqualToString:CHATTER_FEED_DATA] &&
        ![msg.idKey isEqualToString:CHATTER_COMMENTS_DATA]
        )
	{
		if (msg.responseCode == 400)
		{
			// If the session id of the failed message is different than the current session id...
			if (msg.header != nil && ![msg.header isEqualToString:[ExSystem sharedInstance].sessionID])
			{
				// ... then try resending this message with the new session id (the send method will take care of updating it)
				[[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestControl::connectionDidFinishLoading(%@) Received 400 (Bad Request). Session id has already changed.  Retrying request.", msg.idKey] Level:MC_LOG_DEBU];
				[self send];
				return;
			}
			else
			{
                if (msg.options & NO_RETRY)
                    [[PostQueue getInstance] finishPostMsg:self result:@"NETWORK_FAILURE_NO_RETRY"];
                
				[[ApplicationLock sharedInstance] onServerRejectedRequest];
                //CRMC-39820  Home view controller is waiting for the message so that he can hide the toaster
                if ([self.msg.responder.respondToMvc respondsToSelector:@selector(didProcessMessage:)])
                {
                    [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::routeMsgToResponder invoking %@::didProcessMessage:%@", [self.msg.responder.respondToMvc class], msg.idKey] Level:MC_LOG_DEBU];
                    [self.msg.responder.respondToMvc didProcessMessage:msg];
                }
                if ([self.respondToMvc respondsToSelector:@selector(didProcessMessage:)])
                {
                    [[MCLogging getInstance] log:[NSString stringWithFormat:@"RequestController::routeMsgToResponder invoking %@::didProcessMessage:%@", [respondToMvc class], msg.idKey] Level:MC_LOG_DEBU];
                    [respondToMvc didProcessMessage:msg];
                }
                return;
			}
		}
        
		if (msg.responseCode >= 400 && ((msg.options & SILENT_ERROR) == 0)) 
		{
			ErrorData *ed = [[ErrorData alloc] init];
			[ed parseXMLFileAtData:webData];
			
            if([ed.errors count] == 1)
			{
				(ed.error)[@"ErrorCode"] = msg.errCode;
                msg.errBody = [self translateServerError:(ed.error)[@"Message"]]; // MOB-9400 Handle site down msg
			}
            
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:[NSString stringWithFormat:@"Error %@", msg.errCode]
								  message:msg.errBody
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];

			[alert show];
		}
	}
    else if (([msg.idKey isEqualToString:AUTHENTICATION_DATA] || 
              [msg.idKey isEqualToString:UPLOAD_IMAGE_DATA] || 
              [msg.idKey isEqualToString:RECEIPT_STORE_RECEIPTS] ||  
              [msg.idKey isEqualToString:CORP_SSO_AUTHENTICATION_DATA] ) && msg.responseCode >= 400)
    {   // MOB-7663 Capture Pin Expired msg.
        ErrorData *ed = [[ErrorData alloc] init];
        [ed parseXMLFileAtData:webData];
        
        if([ed.errors count] == 1)
        {
            msg.errBody = [self translateServerError :(ed.error)[@"Message"]]; // MOB-9400 Handle site down msg
        }
        
    }

	// Remove post msg from queue
	if ([self.msg.method isEqualToString:@"POST"])
	{
		[[PostQueue getInstance] finishPostMsg:self result:@"NORMAL"];
	}
	
#ifdef SLOWMO

	int i;
	BOOL isRunning = YES;
	for (i = 0; (isRunning && i < 3); i++) {
		[[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode
								 beforeDate:[NSDate dateWithTimeIntervalSinceNow:1.]];
	}

#endif
	
	UIApplication* app = [UIApplication sharedApplication]; 
	app.networkActivityIndicatorVisible = NO;
	
	//so, parse the xml data and then return the message, with the obj
	self.msg.data = webData;		//fill the message with this data

    [self routeMsgToResponderReceivedResponse:YES];
}


- (void)connection:(NSURLConnection *)connection didSendBodyData:(NSInteger)bytesWritten totalBytesWritten:(NSInteger)totalBytesWritten totalBytesExpectedToWrite:(NSInteger)totalBytesExpectedToWrite
{
    NSNumber *writtenBytes = @(totalBytesWritten);
    NSNumber *bytesExpectedToWrite = @(totalBytesExpectedToWrite);
    NSDictionary  *userInfoDict = @{@"TOTAL_WRITTEN_BYTES": writtenBytes, @"TOTAL_BYTES_EXPECTED_TO_WRITE": bytesExpectedToWrite};

    
    NSNotification *notif = [NSNotification notificationWithName:MVC_CONNECTION_PROGRESS_MESSAGE object:nil userInfo:userInfoDict];
	[[NSNotificationCenter defaultCenter] postNotification:notif];
}

- (NSCachedURLResponse *)connection:(NSURLConnection *)connection willCacheResponse:(NSCachedURLResponse *)cachedResponse {
    return nil;
}

#pragma mark Memory Methods


+(void) retrieveImageFromUrl: (NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc
{
	[self retrieveImageFromUrl:imageUrl MsgId:msgId SessionID:sessionID MVC:mvc ParameterBag:nil];
}


+(void) retrieveImageFromUrl: (NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag
{
	RequestController *rc = [RequestController alloc];	
	Msg *msgImage = [[Msg alloc] initWithData:msgId State:@"" Position:nil MessageData:nil URI:imageUrl MessageResponder:nil ParameterBag:pBag];
	
	[msgImage setHeader:sessionID];
	[msgImage setContentType:@"application/xml"];
    [msgImage setAcceptHeader:@"application/xml;application/pdf"];
	[msgImage setMethod:@"GET"];
	
	[rc initDirect:msgImage MVC:mvc];				
}


+(void) retrieveImageFromUrlWithoutCaching:(NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag
{
	RequestController *rc = [RequestController alloc];	
	Msg *msgImage = [[Msg alloc] initWithDataAndSkipCache:msgId State:@"" Position:nil MessageData:nil URI:imageUrl MessageResponder:nil ParameterBag:pBag];
	
	[msgImage setHeader:sessionID];
	[msgImage setContentType:@"application/xml"];
    [msgImage setAcceptHeader:@"application/xml;application/pdf"];
	[msgImage setMethod:@"GET"];
	
	[rc initDirect:msgImage MVC:mvc];				
	
}

+(void) retrieveReportPDFImageFromUrlNoCaching:(NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag
{
	RequestController *rc = [RequestController alloc];	
	Msg *msgImage = [[Msg alloc] initWithDataAndSkipCache:msgId State:@"" Position:nil MessageData:nil URI:imageUrl MessageResponder:nil ParameterBag:pBag];
	
	[msgImage setHeader:sessionID];
	[msgImage setContentType:@"application/xml"];
    [msgImage setAcceptHeader:@"application/xml;application/pdf"];
	[msgImage setMethod:@"POST"];
	
	[rc initDirect:msgImage MVC:mvc];				
	
}

+(void) retrieveImageUrlFromGUID:(NSString*)guid MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag
{
	RequestController *rc = [RequestController alloc];	
	Msg *msgImage = [[Msg alloc] initWithDataAndSkipCache:msgId State:@"" Position:nil MessageData:nil URI:guid MessageResponder:nil ParameterBag:pBag];
	
	[msgImage setHeader:sessionID];
	[msgImage setContentType:@"application/xml"];
	[msgImage setMethod:@"GET"];
	
	[rc initDirect:msgImage MVC:mvc];				
}


-(void) getCurrentSettings
{
    
}

@end
