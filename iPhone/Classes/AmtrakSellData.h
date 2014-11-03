//
//  AmtrakSellData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface AmtrakSellData : NSObject {
	NSString	*sellStatus, *tripLocator;
    NSString    *authorizationNumber;
}

@property (strong, nonatomic) NSString *sellStatus;
@property (strong, nonatomic) NSString *tripLocator;
@property (nonatomic, strong) NSString *authorizationNumber;
@property (nonatomic, strong) NSString *itinLocator;
@property (nonatomic, strong) NSString *errorMessage;

@end
