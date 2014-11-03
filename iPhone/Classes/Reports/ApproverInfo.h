//
//  ApproverInfo.h
//  ConcurMobile
//
//  Created by yiwen on 8/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>



@interface ApproverInfo : NSObject 
{
	NSString			*email;
	NSString			*empKey;  
	NSString			*firstName;  
	NSString			*lastName;
	NSString			*loginId;  
}

@property (strong, nonatomic) NSString *email;
@property (strong, nonatomic) NSString *empKey;
@property (strong, nonatomic) NSString *firstName;
@property (strong, nonatomic) NSString *lastName;
@property (strong, nonatomic) NSString *loginId;

@end
