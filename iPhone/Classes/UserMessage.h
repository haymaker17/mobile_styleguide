//
//  UserMessage.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface UserMessage : NSObject {
	NSString *msgTitle;
	NSString *msgBody;
	NSString *msgURL;
//	int		 msgID;
}

@property (nonatomic ,strong) NSString *msgTitle;
@property (nonatomic ,strong) NSString *msgBody;
@property (nonatomic ,strong) NSString *msgURL;
//@property (nonatomic ,assign) int		 msgID;
@end
