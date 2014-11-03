#import <Foundation/Foundation.h>
#import "FBConnect/FBConnect.h"
#import "MOFBPermission.h"

@protocol MOFBStatusDelegate <NSObject>
@optional
-(void)statusDidUpdate:(id) status;
-(void)status:(id) status DidFailWithError:(NSError*)error;
@end

@interface MOFBStatus : NSObject <FBRequestDelegate, MOFBPermissionDelegate> {
	id delegate;
	NSString *status;
}

@property (nonatomic,assign) id <MOFBStatusDelegate> delegate;
@property (nonatomic,retain) NSString *status;

- (void)update:(NSString *)string;


@end
