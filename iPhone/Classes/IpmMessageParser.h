//
//  IpmMessageParser.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface IpmMessageParser : NSObject

@property (nonatomic, readonly, strong) NSArray *messages;

- (id)initWithXmlResponse:(NSString *)response;

@end
