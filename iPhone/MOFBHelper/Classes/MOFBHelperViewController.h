#import <UIKit/UIKit.h>
#import "FBConnect/FBConnect.h"
#import "MOFBHelper.h"

@interface MOFBHelperViewController : UIViewController <FBSessionDelegate, MOFBHelperDelegate> {
	FBSession *session;
	MOFBHelper *fbHelper;
}

@end
