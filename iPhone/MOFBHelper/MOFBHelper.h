#import <Foundation/Foundation.h>
#import "MOFBStatus.h"

@class MOFBHelper;

@protocol MOFBHelperDelegate <MOFBStatusDelegate>
@end

@interface MOFBHelper : NSObject <MOFBStatusDelegate>{
	id delegate;
	MOFBStatus *mofbStatus;
}

@property (nonatomic, assign) id <MOFBHelperDelegate> delegate;
@property (nonatomic, retain) NSString *status;

@end
