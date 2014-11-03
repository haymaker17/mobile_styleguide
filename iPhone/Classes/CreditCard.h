//
//  CreditCard.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CreditCard : NSObject
{
	NSString            *ccId;
	NSString            *maskedNumber;
	NSString            *name;
	NSUInteger          type;
    NSMutableDictionary *allowedUses;
    NSMutableDictionary *defaultUses;
}

@property (nonatomic, strong) NSString              *ccId;
@property (nonatomic, strong) NSString              *maskedNumber;
@property (nonatomic, strong) NSString              *name;
@property (nonatomic) NSUInteger                    type;
@property (nonatomic, strong) NSMutableDictionary   *allowedUses;
@property (nonatomic, strong) NSMutableDictionary   *defaultUses;

@end
