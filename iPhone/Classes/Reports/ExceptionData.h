//
//  ExceptionData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ExceptionData : NSObject 
{
	NSString		*exceptionsStr, *severityLevel;
}

@property (strong, nonatomic) NSString *exceptionsStr;
@property (strong, nonatomic) NSString *severityLevel;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

@end
