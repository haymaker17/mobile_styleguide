//
//  ViewStateDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol ViewStateDelegate <NSObject>
-(UIViewController*) viewController;
// TODO - Fix ipad wait view and kill this one
-(BOOL)neediPadWaitViews;

@end
