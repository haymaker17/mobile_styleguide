//
//  FlowCoverViewController.h
//  FlowCover
//
//  Created by William Woody on 12/13/08.
//  Copyright __MyCompanyName__ 2008. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FlowCoverView.h"
#import	"ReceiptManagerFolderViewController.h"

@interface FlowCoverViewController : UIViewController <FlowCoverViewDelegate>
{
	NSMutableArray			*imageData;
	int						coverFlowImageIndex;
	ReceiptManagerFolderViewController	*rm;
}

- (IBAction)done:(id)sender;
@property (nonatomic, retain) NSMutableArray *imageData;
@property int coverFlowImageIndex;
@property (retain, nonatomic) ReceiptManagerFolderViewController	*rm;

@end

