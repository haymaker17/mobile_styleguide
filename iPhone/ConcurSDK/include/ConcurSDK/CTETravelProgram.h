//
//  CTETravelProgram.h
//  ConcurSDK
//
//  Created by Christopher Butcher on 30/07/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTETravelProgram : NSObject

@property (nonatomic, strong, readwrite) NSString* programDescription;
@property (nonatomic, strong, readwrite) NSString* programId;
@property (nonatomic, strong, readwrite) NSString* name;
@property (nonatomic, strong, readwrite) NSString* programType;
@property (nonatomic, strong, readwrite) NSString* vendor;
@property (nonatomic, strong, readwrite) NSString* vendorCode;
@property (nonatomic, readwrite) BOOL expectedSelection;

-(id)initWithJSonResponse:(NSDictionary *)response;
+ (NSArray*)parseListOfTravelPrograms:(NSDictionary *)jsonResponse;

@end
