#import "MOFBStatus.h"
#import "MOFBErrors.h"

@implementation MOFBStatus

@synthesize delegate, status;

- (void)update:(NSString *)string {
	self.status = string;
	MOFBPermission *permission = [[[MOFBPermission alloc] init] retain];
	permission.delegate = self;
	[permission obtain:@"status_update"];
}

- (void)permissionGranted:(MOFBPermission*)permission {
	[permission release];
	NSDictionary *params = [NSDictionary dictionaryWithObjectsAndKeys: self.status, @"status", @"true", @"status_includes_verb", nil];
	[[FBRequest requestWithDelegate:self] call:@"facebook.Users.setStatus" params:params];	
}

- (void)permissionDenied:(MOFBPermission*)permission {
	[permission release];
	NSDictionary *userInfo = [NSDictionary dictionaryWithObjectsAndKeys: @"Permission denied", NSLocalizedDescriptionKey, nil];
	NSError *error = [NSError errorWithDomain:@"MOFBErrorDomain" code:PERMISSION_DENIED userInfo:userInfo];
	[delegate status:self DidFailWithError:error]; 
}

- (void)request:(FBRequest*)request didLoad:(NSString *)result {
	if ([result isEqualToString:@"1"])
		[delegate statusDidUpdate:self];
	else {
		NSDictionary *userInfo = [NSDictionary dictionaryWithObjectsAndKeys: @"API returned failure status", NSLocalizedDescriptionKey, nil];
		NSError *error = [NSError errorWithDomain:@"MOFBErrorDomain" code:API_FAILURE userInfo:userInfo];
		[delegate status:self DidFailWithError:error]; 
	}
}

- (void)dealloc {
	[status release];
	[super dealloc];
}

@end
