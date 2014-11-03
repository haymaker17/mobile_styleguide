#import <Foundation/Foundation.h>
#import "FBConnect/FBConnect.h"

@class MOFBPermission;

@protocol MOFBPermissionDelegate <NSObject>
@optional
- (void)permissionGranted:(MOFBPermission*)permission;
- (void)permissionDenied:(MOFBPermission*)permission;
@end

@interface MOFBPermission : NSObject <FBRequestDelegate, FBDialogDelegate> {
	id delegate;
	NSString *extPerm;
}

@property (nonatomic, assign) id <MOFBPermissionDelegate> delegate;
@property (nonatomic, retain) NSString *extPerm;

- (void)obtain:(NSString *)extPerm;

@end
