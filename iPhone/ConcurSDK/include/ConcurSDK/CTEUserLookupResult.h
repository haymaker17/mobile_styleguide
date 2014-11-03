//
//  CTEUserLookupResult.h
//  ConcurSDK
//
//  Created by ernest cho on 3/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEUserLookupResult : NSObject

// Concur username, often this is the work email that was submitted
@property (nonatomic, readonly, strong) NSString *username;
@property (nonatomic, readonly, strong) NSString *email;

// Concur web service server url
@property (nonatomic, readonly, strong) NSString *concurWebServiceURL;

// Single sign on url
@property (nonatomic, readonly, strong) NSString *companySingleSignOnURL;

// user login type information
@property (nonatomic, readonly, assign) BOOL isSingleSignOnUser;
@property (nonatomic, readonly, assign) BOOL isPasswordUser;
@property (nonatomic, readonly, assign) BOOL isMobilePasswordUser;

// did we successfully parse the xml?
@property (nonatomic, assign) BOOL isSuccessful;

- (id)initWithResponseXML:(NSString *)xml;

@end
