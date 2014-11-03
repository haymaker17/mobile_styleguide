//
//  CTESingleSignOnWebView.h
//  ConcurSDK
//
//  Created by ernest cho on 2/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEError.h"

@interface CTESingleSignOnWebView : UIWebView <UIWebViewDelegate>

/**
 Loads the company single sign on webpage.  
 Logs into Concur if the user successfully logs into the company single sign on webpage.
 
 @param url
        Company Single Sign On URL
 @param tokenFound
        Token found callback block.  This informs the client that the token has been found and login is in progress.  It's ok to block the webview.
 @param success
        Success callback block
 @param failure
        Failure callback block
 */
- (void)loadSingleSignOnWebpage:(NSString *)url
                     tokenFound:(void (^)())tokenFound
                        success:(void (^)())success
                        failure:(void (^)(CTEError *error))failure;

/**
 Loads the company single sign on webpage.
 Logs into Concur if the user successfully logs into the company single sign on webpage.

 @param url
        Company Single Sign On URL
 @param tokenFound
        Token found callback block.  This informs the client that the token has been found and login is in progress.  It's ok to block the webview.
 @param success
        Success callback block.  Includes the login XML.
 @param failure
        Failure callback block
 */
- (void)loadConcurMobileSingleSignOnWebpage:(NSString *)url
                                 tokenFound:(void (^)())tokenFound
                                    success:(void (^)(NSString *loginXML))success
                                    failure:(void (^)(CTEError *error))failure;

@end
