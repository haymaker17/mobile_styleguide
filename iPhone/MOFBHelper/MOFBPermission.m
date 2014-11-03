#import "MOFBPermission.h"

@implementation MOFBPermission

@synthesize delegate, extPerm;

- (void)obtain:(NSString *)anExtPerm {
	self.extPerm = anExtPerm;
	NSDictionary *params = [NSDictionary dictionaryWithObjectsAndKeys: self.extPerm, @"ext_perm", nil];
	[[FBRequest requestWithDelegate:self] call:@"facebook.Users.hasAppPermission" params:params];			
}

- (void)request:(FBRequest*)request didLoad:(NSString *)result {
	if ([result isEqualToString:@"1"])
		[delegate permissionGranted: self];
	else {
		FBPermissionDialog* dialog = [[[FBPermissionDialog alloc] init] autorelease];
		dialog.delegate = self;
		dialog.permission = self.extPerm;
		[dialog show];
	}
}

- (void)dialogDidSucceed:(FBDialog*)dialog {
	[delegate permissionGranted:self];
}

- (void)dialogDidCancel:(FBDialog*)dialog {
	[delegate permissionDenied:self];
}

- (void)dealloc {
	[extPerm release];
	[super dealloc];
}

@end
