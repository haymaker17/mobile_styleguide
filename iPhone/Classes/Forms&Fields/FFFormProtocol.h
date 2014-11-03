//
//  FFFormProtocol.h
//  ConcurMobile
//
//  Created by laurent mery on 01/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol FFFormProtocol <NSObject>

-(void)becomeDirty;
-(void)pushEditViewController:(UIViewController*)editViewController;

@end
