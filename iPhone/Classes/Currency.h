//
//  Currency.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface Currency : NSObject 
{
	NSString		*crnCode, *crnName, *decimalDigits;

}

@property (nonatomic, strong) NSString		*crnCode;
@property (nonatomic, strong) NSString		*crnName;
@property (nonatomic, strong) NSString		*decimalDigits;

@end
