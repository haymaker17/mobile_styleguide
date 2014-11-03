//
//  UploadQueueAlertView.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileAlertView.h"
#import "UploadQueueDelegate.h"

@interface UploadQueueAlertView : MobileAlertView <UploadQueueDelegate>
{
    BOOL didClickCancelUploadBtn;
}
@property (nonatomic, assign) BOOL showSpinner;
@property (nonatomic, assign) BOOL needUpdateMsg;
@property BOOL didClickCancelUploadBtn;

@property (nonatomic, strong) UIActivityIndicatorView *UISpinner;

-(id) initForUpload;
-(void) invokeNonCancellableAlertView:(NSString*)title;

@end
