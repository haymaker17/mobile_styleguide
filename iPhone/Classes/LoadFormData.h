//
//  LoadFormData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@interface LoadFormData : MsgResponder
{
	NSString				*path;
}

@property (nonatomic, strong) NSString					*path;

@end
