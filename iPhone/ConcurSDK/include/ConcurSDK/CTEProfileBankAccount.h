//
//  CTEProfileBankAccount.h
//  ConcurSDK
//
//  Created by Ray Chi on 12/15/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEProfileBankAccount : NSObject

@property (nonatomic,strong,readonly) NSString *accountNo;
@property (nonatomic,strong,readonly) NSString *routingNo;
@property (nonatomic,strong,readonly) NSString *type;
@property (nonatomic,strong,readonly) NSString *status;

/**
 *  Initialize with server JSON
 */
- (id)initWithJson:(NSDictionary *)json;

/**
 *  Initialize the class with user's input
 */
- (id)initWithAccountNo:(NSString *)accountNo
              routingNo:(NSString *)routingNo
                   type:(NSString *)type
                 status:(NSString *)status;

@end
