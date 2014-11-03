//
//  ExSiteSetting.h
//  ConcurMobile
//
//  Created by yiwen on 3/11/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ExSiteSetting : NSObject {
	NSString	*name, *type, *value;

}

@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSString *type;
@property (strong, nonatomic) NSString *value;

@end
