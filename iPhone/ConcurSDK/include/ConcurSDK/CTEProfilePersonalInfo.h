//
//  CTEProfilePersonalInfo.h
//  ConcurSDK
//
//  Created by Ray Chi on 12/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEProfilePersonalInfo : NSObject

@property (nonatomic,strong,readonly) NSString *firstName;
@property (nonatomic,strong,readonly) NSString *lastName;
@property (nonatomic,strong,readonly) NSString *phoneNo;
@property (nonatomic,strong,readonly) NSString *workAddress;
@property (nonatomic,strong,readonly) NSString *city;
@property (nonatomic,strong,readonly) NSString *state;
@property (nonatomic,strong,readonly) NSString *country;
@property (nonatomic,strong,readonly) NSString *zipCode;

/**
 *  Initialize with server JSON
 */
- (id)initWithJson:(NSDictionary *)json;

/**
 *  Initialize with user's input
 */
- (id)initWithFirstName:(NSString *)firstName
               lastName:(NSString *)lastName
                phoneNo:(NSString *)phoneNo
            workAddress:(NSString *)workAddress
                   city:(NSString *)city
                  state:(NSString *)state
                country:(NSString *)country
                zipCode:(NSString *)zipCode;

@end
