//
//  UploadQueueViewController.h
//  ConcurMobile
//
//  Created by Shifan Wu on 10/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UploadQueueDS.h"
#import "UploadQueueDSDelegate.h"
#import "UploadQueueVCDelegate.h"

@interface UploadQueueViewController : MobileViewController <UploadQueueDSDelegate, UploadQueueVCDelegate>
{
    UploadQueueDS *ds;

    id<UploadQueueVCDelegate>   __weak _delegate;
}
@property (strong, nonatomic) IBOutlet UITableView *tableList;
@property (strong, nonatomic) UploadQueueDS *ds;

@property (nonatomic, weak) id<UploadQueueVCDelegate> delegate;

-(void) startUpload;
@end
