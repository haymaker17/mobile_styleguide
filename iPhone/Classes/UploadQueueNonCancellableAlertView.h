//
//  UploadQueueNonCancellableAlertView.h
//  ConcurMobile
//
//  Created by charlottef on 11/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UploadQueueDelegate.h"

@interface UploadQueueNonCancellableAlertView : MobileAlertView <UploadQueueDelegate, UIAlertViewDelegate>
{
}

@property (nonatomic, assign) BOOL showSpinner;

@property (nonatomic, strong) UIActivityIndicatorView *UISpinner;

-(id) initWithTitle:(NSString*)title message:(NSString*)message;


@end
